package com.home.network.statistic.poller.tplink.deco.out.etl;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApInfo {
    private Long apMac;
    private String apName;

    public Object[] toRowMap() {
        return new Object[]{apMac, apName};
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApInfo apInfo = (ApInfo) o;
        return Objects.equals(apMac, apInfo.apMac) && Objects.equals(apName, apInfo.apName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apMac, apName);
    }
}
