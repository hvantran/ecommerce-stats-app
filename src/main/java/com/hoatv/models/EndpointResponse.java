package com.hoatv.models;

import com.hoatv.controllers.EndpointResponseVO;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(columnList = "column1"))
public class EndpointResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "endpointConfigId", nullable = false)
    private EndpointSetting endpointSetting;

    @Column
    private String column1;
    @Column(length = 1024)
    private String column2;
    @Column(length = 1024)
    private String column3;
    @Column
    private String column4;
    @Column
    private String column5;
    @Column
    private String column6;
    @Column
    private String column7;
    @Column
    private String column8;
    @Column(length = 1024)
    private String column9;
    @Column(length = 1024)
    private String column10;

    public EndpointResponseVO toEndpointResponseVO() {
        return EndpointResponseVO.builder()
                .id(id)
                .column1(column1)
                .column2(column2)
                .column3(column3)
                .column4(column4)
                .column5(column5)
                .column6(column6)
                .column7(column7)
                .column8(column8)
                .column9(column9)
                .column10(column10)
                .build();
    }
}
