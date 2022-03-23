package com.hoatv;

import com.hoatv.fwk.common.services.HttpClientFactory;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.annotations.MetricRegistry;
import com.hoatv.metric.mgmt.services.MetricProviderRegistry;
import com.hoatv.providers.Tiki;
import com.hoatv.repositories.ProductRepository;
import com.hoatv.services.ProductService;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.entities.TaskCollection;
import com.hoatv.task.mgmt.services.ScheduleTaskMgmtService;
import com.hoatv.task.mgmt.services.ScheduleTaskRegistryService;
import com.hoatv.task.mgmt.services.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
    @Bean
    ServletListenerRegistrationBean<ServletContextListener> servletListener() {
        ServletListenerRegistrationBean<ServletContextListener> srb = new ServletListenerRegistrationBean<>();
        srb.setListener(new ApplicationServletContextListener());
        return srb;
    }

    @Bean(destroyMethod = "destroy")
    public ScheduleTaskRegistryService getScheduleTaskRegistryService() {
        return new ScheduleTaskRegistryService();
    }

    @Bean
    public ProductService getTikiProductService(ProductRepository productRepository, Tiki tiki){
        return new ProductService(productRepository, tiki);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            LOGGER.info("Let's inspect the beans provided by Spring Boot");
            String[] metricProviderBeanNames = ctx.getBeanNamesForAnnotation(MetricProvider.class);
            String[] metricRegistryBeanNames = ctx.getBeanNamesForAnnotation(MetricRegistry.class);
            String[] schedulePoolSettingBeanNames = ctx.getBeanNamesForAnnotation(SchedulePoolSettings.class);

            List<Object> metricProviders = Stream.of(metricProviderBeanNames).map(ctx::getBean).collect(Collectors.toList());
            List<Object> metricRegistries = Stream.of(metricRegistryBeanNames).map(ctx::getBean).collect(Collectors.toList());
            List<Object> poolSettings = Stream.of(schedulePoolSettingBeanNames).map(ctx::getBean).collect(Collectors.toList());
            ObjectUtils.checkThenThrow(metricRegistries.size() != 1, "Required at least one of Metric Registry annotation");
            Object metricRegistry = metricRegistries.stream().findFirst().orElseThrow();
            ObjectUtils.checkThenThrow(!(metricRegistry instanceof MetricProviderRegistry), "This must be an instance of MetricProviderRegistry");
            MetricProviderRegistry metricProviderRegistry = (MetricProviderRegistry) metricRegistry;
            metricProviderRegistry.loadFromObjects(metricProviders);

            ScheduleTaskRegistryService scheduleTaskExecutorService = ctx.getBean(ScheduleTaskRegistryService.class);

            poolSettings.forEach(applicationObj -> {
                SchedulePoolSettings schedulePoolSettings = applicationObj.getClass().getAnnotation(SchedulePoolSettings.class);
                ScheduleTaskMgmtService taskMgmtExecutorV1 = TaskFactory.INSTANCE.newScheduleTaskMgmtService(schedulePoolSettings);

                scheduleTaskExecutorService.register(schedulePoolSettings.application(), taskMgmtExecutorV1);
                TaskCollection taskCollection = TaskCollection.fromObject(applicationObj);
                taskMgmtExecutorV1.scheduleTasks(taskCollection, 5, TimeUnit.SECONDS);
            });

            ProductService productService = ctx.getBean(ProductService.class);
            productService.init();
        };
    }

    public static class ApplicationServletContextListener implements ServletContextListener {

        @Override
        public void contextDestroyed(ServletContextEvent event) {
            LOGGER.info("Application context is destroyed");
            HttpClientFactory.INSTANCE.destroy();
            TaskFactory.INSTANCE.destroy();
        }

        @Override
        public void contextInitialized(ServletContextEvent event) {
            LOGGER.info("Application context is initialized");
        }
    }
}
