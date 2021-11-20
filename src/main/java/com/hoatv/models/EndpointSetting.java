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
@RequiredArgsConstructor
public class EndpointSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    @NonNull
    private String application;

    @Column
    @NonNull
    private String taskName;

    @NonNull
    @Column(nullable = false)
    private String extEndpoint;

    @NonNull
    @Column(nullable = false)
    private String method;

    @Lob
    @Column
    @NonNull
    private String data;

    @Column
    @NonNull
    private Integer noParallelThread;

    @Lob
    @NonNull
    @Column(nullable = false)
    private String columnMetadata;

    @Column
    @NonNull
    private String generatorMethodName;

    @Column
    @NonNull
    private Integer generatorSaltLength;

    @Column
    @NonNull
    private String generatorSaltStartWith;

    @Column
    @NonNull
    private String successCriteria;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "endpointSetting")
    @ToString.Exclude
    private Set<EndpointResponse> resultSet = new HashSet<>();

    public EndpointSettingVO toEndpointConfigVO() {
        return new EndpointSettingVO(application, taskName, extEndpoint, method, data, noParallelThread, columnMetadata,
                generatorMethodName, generatorSaltLength, generatorSaltStartWith, successCriteria);
    }

    public static EndpointSetting fromEndpointConfigVO(EndpointSettingVO endpointSettingVO) {
        return new EndpointSetting(endpointSettingVO.getApplication(), endpointSettingVO.getTaskName(),
                endpointSettingVO.getExtEndpoint(), endpointSettingVO.getMethod(), endpointSettingVO.getData(),
                endpointSettingVO.getNoParallelThread(), endpointSettingVO.getColumnMetadata(),
                endpointSettingVO.getGeneratorMethodName(), endpointSettingVO.getGeneratorSaltLength(),
                endpointSettingVO.getGeneratorSaltStartWith(), endpointSettingVO.getSuccessCriteria());
    }
}

