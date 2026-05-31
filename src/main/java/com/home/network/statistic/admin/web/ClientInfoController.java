package com.home.network.statistic.admin.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/web/client")
@RequiredArgsConstructor
@Profile({"dev-admin", "prd-admin"})
public class ClientInfoController {
    private final DeviceDimRepo deviceDimRepo;
    private final DeviceWlanConnectionsFactRepo deviceWlanConnectionsFactRepo;
    
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping("/getList.do")
    @Transactional(value = "appJpaTx", readOnly = true)
    public String getClientList(Model model,
                                @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer size) {
        page = page == null ? 1 : Math.max(1, page);
        int pageSize = resolvePageSize(size);

        Pageable defaultPage = PageRequest.of(page - 1, pageSize);
        Page<DeviceDimDTO> allCLients = deviceDimRepo.findAll(defaultPage).map(DeviceDimDTO::new);

        model.addAttribute("clients", allCLients);

        return "clientList";
    }

    private static int resolvePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(1, size), MAX_PAGE_SIZE);
    }

    @GetMapping("/connection/getInfo.do")
    public String getClientConnInfo(Model model,
                                    @RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer size,
                                    @RequestParam(required = false) Integer clientKey) {
        page = page == null ? 1 : Math.max(1, page);
        int pageSize = resolvePageSize(size);

        Sort sort = Sort.by("deviceKey").ascending()
                        .and(Sort.by("eventTimestamp").descending());
        Pageable defaultPage = PageRequest.of(page - 1, pageSize, sort);
        Page<DeviceWlanConnectionsDTO> connections =
            Optional.ofNullable(clientKey)
                    .map(key ->
                            deviceWlanConnectionsFactRepo
                            .findByKeyDeviceKey(clientKey, defaultPage))
                    .orElseGet(() -> deviceWlanConnectionsFactRepo.findAll(defaultPage))
            .map(DeviceWlanConnectionsDTO::new);

        model.addAttribute("connections", connections);

        return "clientConnInfo :: connectionsFragment";
    }
}
