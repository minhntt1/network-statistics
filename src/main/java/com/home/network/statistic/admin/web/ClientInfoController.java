package com.home.network.statistic.admin.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    
    @GetMapping("/getList.do")
    @Transactional(value = "appJpaTx", readOnly = true)
    public String getClientList(Model model,
                                @RequestParam(required = false) Integer page) {
        page = page == null ? 1 : Math.max(1, page);

        Pageable defaultPage = PageRequest.of(page - 1, 15);
        Page<DeviceDimDTO> allCLients = deviceDimRepo.findAll(defaultPage).map(DeviceDimDTO::new);

        model.addAttribute("clients", allCLients);

        return "clientList";
    }

    @GetMapping("/connection/getInfo.do")
    public String getClientConnInfo(Model model,
                                    @RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer clientKey) {
        page = page == null ? 1 : Math.max(1, page);

        Pageable defaultPage = PageRequest.of(page - 1, 15);
        Page<DeviceWlanConnectionsDTO> connections =
            Optional.ofNullable(clientKey)
                    .map(key ->
                            deviceWlanConnectionsFactRepo
                            .findByKeyDeviceKey(clientKey, defaultPage))
                    .orElse(deviceWlanConnectionsFactRepo.findAll(defaultPage))
            .map(DeviceWlanConnectionsDTO::new);

        model.addAttribute("connections", connections);

        return "clientConnInfo :: connectionsFragment";
    }
}
