package com.home.network.statistic.poller.igate.gw240.in;

import com.home.network.statistic.poller.igate.gw240.out.StatusWifiStationEntity;
import com.home.network.statistic.poller.igate.gw240.out.StatusWifiStationRaw;
import com.home.network.statistic.poller.rfc1213.in.SnmpIfTableResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class WebResponse {
    private String content;

    /**
     * filter redundant content in raw to reduce db size
     */
    public void filterRedundantContent() {
        var contentAr = content.split("\n");
        this.content = contentAr[43];   // raw data on line 43
    }

    /**
     * to status entity to save to db
     * @param response snmp response
     * @return entity containing raw web + snmp response in json and poll time
     */
    public StatusWifiStationEntity toStatusWifiStationEntity(List<SnmpIfTableResponse> response) {
        filterRedundantContent();

        return StatusWifiStationRaw.builder()
                .webResponse(content)
                .pollTime(LocalDateTime.now())
                .listRaw(response.stream().map(SnmpIfTableResponse::toSnmpIfTablePhyInfoResponseRaw).toList())
                .build()
                .toStatusWifiStationEntity();
    }
}
