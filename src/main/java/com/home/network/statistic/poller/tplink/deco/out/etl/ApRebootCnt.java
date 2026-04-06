package com.home.network.statistic.poller.tplink.deco.out.etl;

import lombok.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApRebootCnt {
    private String startWeek;
    private ApInfo apInfo;
    private IpNormalized ipNormalized;
    private Integer rebootCnt;

    public Object[] toInsertableRow() {
        return new Object[] {startWeek, apInfo.getApMac(), apInfo.getApName(), ipNormalized.getIpv4(), rebootCnt};
    }

    public static List<Object[]> toListInsertable(Collection<ApRebootCnt> rebootCnts) {
        return rebootCnts.stream()
                .map(ApRebootCnt::toInsertableRow)
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApRebootCnt that = (ApRebootCnt) o;
        return Objects.equals(startWeek, that.startWeek) && Objects.equals(apInfo, that.apInfo) && Objects.equals(ipNormalized, that.ipNormalized);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startWeek, apInfo, ipNormalized);
    }

    public void increaseRebootCnt() {
        this.rebootCnt++;
    }

    public static String prefixKeyDisconnectState() {
        return "apEvent";
    }

    public static boolean hasPrefixKeyRebootState(String key) {
        return key.startsWith(prefixKeyDisconnectState());
    }

    public String extractKeyRebootState() {
        // ap reboot cnt should not contain ip in state, because if ip change, it can count wrong, although ap doest not reboot
        return "%s_%d.%s".formatted(prefixKeyDisconnectState(), apInfo.getApMac(), apInfo.getApName());
    }
}
