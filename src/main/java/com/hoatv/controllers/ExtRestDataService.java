package com.hoatv.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.controllers.MetadataVO.ColumnMetadataVO;
import com.hoatv.fwk.common.exceptions.AppException;
import com.hoatv.fwk.common.services.*;
import com.hoatv.fwk.common.services.GenericHttpClientPool.ExecutionTemplate;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.models.DecryptUtils;
import com.hoatv.models.EndpointResponse;
import com.hoatv.models.EndpointSetting;
import com.hoatv.models.SaltGeneratorUtils;
import com.hoatv.task.mgmt.entities.TaskEntry;
import com.hoatv.task.mgmt.services.TaskMgmtService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
public class ExtRestDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtRestDataService.class);
    public static final HttpClientService HTTP_CLIENT_SERVICE = HttpClientService.INSTANCE;

    private final ExtEndpointSettingRepository extEndpointSettingRepository;
    private final EndpointResponseRepository endpointResponseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExtRestDataService(ExtEndpointSettingRepository extEndpointSettingRepository,
                              EndpointResponseRepository endpointResponseRepository) {
        this.extEndpointSettingRepository = extEndpointSettingRepository;
        this.endpointResponseRepository = endpointResponseRepository;
    }

    public List<EndpointSettingVO> getAllExtEndpoints(String application) {
        List<EndpointSetting> endpointSettings = new ArrayList<>();
        if (StringUtils.isEmpty(application)) {
            List<EndpointSetting> extEndpointSettingRepositoryAll = extEndpointSettingRepository.findAll();
            endpointSettings.addAll(extEndpointSettingRepositoryAll);
        } else {
            List<EndpointSetting> endpointConfigsByApplication = extEndpointSettingRepository.findEndpointConfigsByApplication(
                    application);
            endpointSettings.addAll(endpointConfigsByApplication);
        }
        return endpointSettings.stream().map(EndpointSetting::toEndpointConfigVO).collect(Collectors.toList());
    }

    public void addExtEndpoint(EndpointSettingVO endpointSettingVO) {
        EndpointSetting endpointSetting = EndpointSetting.fromEndpointConfigVO(endpointSettingVO);
        ExtSupportedMethod extSupportedMethod = ExtSupportedMethod.fromString(endpointSetting.getMethod());
        ObjectUtils.checkThenThrow(Objects::isNull, extSupportedMethod, ExtSupportedMethod.INVALID_SUPPORTED_METHOD);

        extEndpointSettingRepository.save(endpointSetting);
        TaskMgmtService<Object> taskMgmtExecutorV1 = new TaskMgmtService<>(1, 5000);
        TaskEntry mainTaskEntry = new TaskEntry();
        Callable<Object> callable = getEndpointResponseTasks(endpointSetting, endpointSettingVO);
        mainTaskEntry.setTaskHandler(callable);
        mainTaskEntry.setApplicationName("Main");
        mainTaskEntry.setName("Execute get endpoint response");
        taskMgmtExecutorV1.execute(mainTaskEntry);

        LOGGER.info("Endpoint {} is added successfully", endpointSetting.getExtEndpoint());
    }

    private Callable<Object> getEndpointResponseTasks(EndpointSetting endpointSetting, EndpointSettingVO endpointSettingVO) {
        // Job configuration
        String application = endpointSetting.getApplication();
        String taskName = endpointSetting.getTaskName();
        Integer runningTimes = endpointSettingVO.getRunningTimes();
        int noParallelThread = endpointSetting.getNoParallelThread();

        // Ext endpoint configuration
        String extEndpoint = endpointSetting.getExtEndpoint();
        String endpointMethod = endpointSetting.getMethod();
        ExtSupportedMethod extSupportedMethod = ExtSupportedMethod.valueOf(endpointMethod);
        String data = endpointSetting.getData();

        // Generator salt
        String generatorMethodName = endpointSetting.getGeneratorMethodName();
        Integer generatorSaltLength = endpointSetting.getGeneratorSaltLength();
        String generatorSaltStartWith = Optional.ofNullable(endpointSetting.getGeneratorSaltStartWith()).orElse("");

        // Metadata
        String columnMetadata = endpointSetting.getColumnMetadata();
        CheckedSupplier<MetadataVO> columnMetadataVOSup = () -> objectMapper.readValue(columnMetadata, MetadataVO.class);
        MetadataVO metadataVO = columnMetadataVOSup.get();

        // Success criteria
        String successCriteria = endpointSetting.getSuccessCriteria();

        CheckedFunction<String, Method> generatorMethodFunc = getGeneratorMethodFunc(generatorSaltStartWith);

        return () -> {
            final Set<String> cachedCodes = new HashSet<>();
            try (GenericHttpClientPool httpClientPool = new GenericHttpClientPool(noParallelThread, 2000);
                 TaskMgmtService<Object> taskMgmtExecutorV2 = new TaskMgmtService<>(noParallelThread, 5000, application)) {

                for (int index = 0; index < runningTimes; index++) {
                    TaskEntry taskEntry = new TaskEntry();
                    taskEntry.setTaskHandler(() -> {
                        String random = generateRandomValue(generatorMethodName, generatorSaltLength,
                                generatorSaltStartWith, metadataVO, generatorMethodFunc, cachedCodes);
                        if (random == null) return null;

                        ExecutionTemplate<String> executionTemplate = getExecutionTemplate(extEndpoint, extSupportedMethod, data, random);
                        String responseString = httpClientPool.executeWithTemplate(executionTemplate);
                        if (StringUtils.isNotEmpty(responseString) && responseString.contains(successCriteria)) {
                            onSuccessResponse(endpointSetting, metadataVO, random, responseString);
                            LOGGER.warn("value: {}", random);
                        }
                        return responseString;
                    });
                    taskEntry.setApplicationName(application);
                    taskEntry.setName(taskName + " " + index);
                    taskMgmtExecutorV2.execute(taskEntry);
                }
            } finally {
                cachedCodes.clear();
            }
            LOGGER.info("{} is completed successfully.", taskName);
            return null;
        };
    }

    private String generateRandomValue(String generatorMethodName, Integer generatorSaltLength, String generatorSaltStartWith, MetadataVO metadataVO, CheckedFunction<String, Method> generatorMethodFunc, Set<String> cachedCodes) throws IllegalAccessException, InvocationTargetException {
        String random = "";
        if (StringUtils.isNotEmpty(generatorMethodName)) {
            String columnId = metadataVO.getColumnId();
            String endpointResponseMethodName = "existsEndpointResponseBy".concat(StringUtils.capitalize(columnId));
            CheckedSupplier<Method> endpointResponseMethodSup = () -> EndpointResponseRepository.class.getMethod(
                    endpointResponseMethodName, String.class);
            Method endpointResponseMethod = endpointResponseMethodSup.get();
            Method generatorMethod = generatorMethodFunc.apply(generatorMethodName);
            random = (String) generatorMethod.invoke(SaltGeneratorUtils.class, generatorSaltLength,
                    generatorSaltStartWith);
            if (isProcessedBefore(endpointResponseMethod, cachedCodes, random)) {
                return null;
            }
        }
        return random;
    }

    private void onSuccessResponse(EndpointSetting endpointSetting, MetadataVO metadataVO, String random, String responseString) {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(responseString);

        List<ColumnMetadataVO> columnMetadataVOs = metadataVO.getColumnMetadata();
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setEndpointSetting(endpointSetting);
        DocumentContext documentContext = JsonPath.parse(document);
        CheckedConsumer<ColumnMetadataVO> columnVOConsumer = column -> {
            String fieldJsonPath = column.getFieldPath();
            String columnName = StringUtils.capitalize(column.getMappingColumnName());
            String decryptFunctionName = column.getDecryptFunctionName();
            String getMethodName = "set".concat(columnName);
            String value = random;

            if (!fieldJsonPath.equals("random")) {
                value = documentContext.read(fieldJsonPath, String.class);
            }

            if (StringUtils.isNotEmpty(decryptFunctionName)) {
                Method decryptMethod = DecryptUtils.class.getMethod(decryptFunctionName, String.class);
                value = (String) decryptMethod.invoke(DecryptUtils.class, value);
            }

            Method setMethod = EndpointResponse.class.getMethod(getMethodName, String.class);
            setMethod.invoke(endpointResponse, value);
        };
        columnMetadataVOs.forEach(columnVOConsumer);
        endpointResponseRepository.save(endpointResponse);
    }

    private ExecutionTemplate<String> getExecutionTemplate(String extEndpoint, ExtSupportedMethod endpointMethod, String data,
                                                           String random) {
        return httpClient -> {
            switch (endpointMethod) {
                case POST:
                    String fullData = String.format(data, random);
                    HttpResponse<String> httpResponse = HTTP_CLIENT_SERVICE.sendPOSTRequest(httpClient, fullData, extEndpoint);
                    return httpResponse.body();
                case GET:
                    String fullURL = String.format(extEndpoint, random);
                    httpResponse = HTTP_CLIENT_SERVICE.sendGETRequest(httpClient, fullURL);
                    return httpResponse.body();
                default:
                    throw new AppException(ExtSupportedMethod.INVALID_SUPPORTED_METHOD);
            }
        };
    }

    private boolean isProcessedBefore(Method endpointResponseMethod, Set<String> cachedCodes,
                                      String random) throws IllegalAccessException, InvocationTargetException {
        if (cachedCodes.contains(random)) {
            return true;
        }

        cachedCodes.add(random);
        Boolean isAlreadyExisted = (Boolean) endpointResponseMethod.invoke(endpointResponseRepository, random);
        if (isAlreadyExisted.equals(Boolean.TRUE)) {
            LOGGER.warn("Account with id {} is already exist", random);
        }
        return isAlreadyExisted;
    }

    private CheckedFunction<String, Method> getGeneratorMethodFunc(String generatorSaltStartWith) {
        return methodName -> {
            if (StringUtils.isNotEmpty(generatorSaltStartWith)) {
                return SaltGeneratorUtils.class.getMethod(methodName, Integer.class, String.class);
            }
            return SaltGeneratorUtils.class.getMethod(methodName, Integer.class);
        };
    }

    @Transactional
    public List<EndpointResponseVO> getEndpointResponses(String application) {
        List<EndpointSetting> endpointSettings = extEndpointSettingRepository.findEndpointConfigsByApplication(
                application);
        if (endpointSettings.isEmpty()) {
            return Collections.emptyList();
        }
        List<EndpointResponse> responses = endpointResponseRepository.findEndpointResponsesByEndpointSettingIn(
                endpointSettings);
        return responses.stream().map(EndpointResponse::toEndpointResponseVO).collect(Collectors.toList());
    }
}
