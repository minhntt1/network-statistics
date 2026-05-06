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
public class ApConnLostCnt {
    private String startWeek;
    private ApInfo apInfo;
    private IpNormalized ipNormalized;
    private Integer lostCnt;

    public Object[] toInsertableRow() {
        return new Object[] {startWeek, apInfo.getApMac(), apInfo.getApName(), ipNormalized.getIpv4(), lostCnt};
    }

    public static List<Object[]> toListInsertable(Collection<ApConnLostCnt> rebootCnts) {
        return rebootCnts.stream()
                .map(ApConnLostCnt::toInsertableRow)
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApConnLostCnt that = (ApConnLostCnt) o;
        return Objects.equals(startWeek, that.startWeek) && Objects.equals(apInfo, that.apInfo) && Objects.equals(ipNormalized, that.ipNormalized);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startWeek, apInfo, ipNormalized);
    }

    public void increaseLostCnt() {
        this.lostCnt++;
    }

    public static String prefixKeyConnLostState() {
        return "apConnLostEvent";
    }

    public static boolean hasPrefixKeyRebootState(String key) {
        return key.startsWith(prefixKeyConnLostState());
    }

    public String extractKeyConnLostState() {
        // ap reboot cnt should not contain ip in state, because if ip change, it can count wrong, although ap doest not reboot
        return "%s_%d.%s".formatted(prefixKeyConnLostState(), apInfo.getApMac(), apInfo.getApName());
    }
}
