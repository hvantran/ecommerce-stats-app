package com.hoatv.models;

import com.hoatv.controllers.EndpointSettingVO;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String application;

    @Column
    private String taskName;

    @Column(nullable = false)
    private String extEndpoint;

    @Column(nullable = false)
    private String method;

    @Lob
    @Column
    private String data;

    @Column
    private Integer noParallelThread;

    @Lob
    @Column(nullable = false)
    private String columnMetadata;

    @Column
    private String generatorMethodName;

    @Column
    private Integer generatorSaltLength;

    @Column
    private String generatorSaltStartWith;

    @Column
    private String successCriteria;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "endpointSetting")
    @ToString.Exclude
    private Set<EndpointResponse> resultSet = new HashSet<>();

    public EndpointSettingVO toEndpointConfigVO() {
        return new EndpointSettingVO(application, taskName, extEndpoint, method, data, noParallelThread, columnMetadata,
                generatorMethodName, generatorSaltLength, generatorSaltStartWith, successCriteria);
    }

    public static EndpointSetting fromEndpointConfigVO(EndpointSettingVO endpointSettingVO) {
        return EndpointSetting.builder()
                .application(endpointSettingVO.getApplication())
                .taskName(endpointSettingVO.getTaskName())
                .extEndpoint(endpointSettingVO.getExtEndpoint())
                .method(endpointSettingVO.getMethod())
                .data(endpointSettingVO.getData())
                .noParallelThread(endpointSettingVO.getNoParallelThread())
                .columnMetadata(endpointSettingVO.getColumnMetadata())
                .generatorMethodName(endpointSettingVO.getGeneratorMethodName())
                .generatorSaltLength(endpointSettingVO.getGeneratorSaltLength())
                .generatorSaltStartWith(endpointSettingVO.getGeneratorSaltStartWith())
                .successCriteria(endpointSettingVO.getSuccessCriteria())
                .build();
    }
}

