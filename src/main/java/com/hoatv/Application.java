package com.hoatv;

import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.metric.common.providers.MetricProviderRegistry;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.annotations.MetricRegistry;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.entities.TaskCollection;
import com.hoatv.task.schedule.executors.ScheduleTaskMgmtExecutorV1;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println("Let's inspect the beans provided by Spring Boot:");
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

            poolSettings.stream().forEach(applicationObj -> {
                SchedulePoolSettings annotation = applicationObj.getClass().getAnnotation(SchedulePoolSettings.class);
                ScheduleTaskMgmtExecutorV1 taskMgmtExecutorV1 = new ScheduleTaskMgmtExecutorV1(annotation);

                TaskCollection taskCollection = TaskCollection.fromObject(applicationObj);
                taskMgmtExecutorV1.execute(taskCollection);
            });
        };
    }
}
