package com.hoatv.controllers;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EndpointSettingVO {

    private String application;
    private String taskName;

    private String extEndpoint;
    private String method;
    private String data;

    private Integer runningTimes;
    private Integer noParallelThread;

    private String columnMetadata;

    private String generatorMethodName;
    private Integer generatorSaltLength;
    private String generatorSaltStartWith;
    private String successCriteria;

    public EndpointSettingVO(String application, String taskName, String extEndpoint, String method, String data,
                             Integer noParallelThread, String columnMetadata, String generatorMethodName,
                             Integer generatorSaltLength, String generatorSaltStartWith, String successCriteria) {
        this.application = application;
        this.taskName = taskName;
        this.extEndpoint = extEndpoint;
        this.method = method;
        this.data = data;
        this.noParallelThread = noParallelThread;
        this.columnMetadata = columnMetadata;
        this.generatorMethodName = generatorMethodName;
        this.generatorSaltLength = generatorSaltLength;
        this.generatorSaltStartWith = generatorSaltStartWith;
        this.successCriteria = successCriteria;
    }
}

