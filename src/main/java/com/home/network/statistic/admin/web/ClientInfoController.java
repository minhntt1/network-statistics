package com.home.network.statistic.admin.web;

import com.home.network.statistic.common.model.ListSqlQuery;
import com.home.network.statistic.common.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/web/client")
@Profile({"dev-admin", "prd-admin"})
public class ClientInfoController {
    private final DeviceDimRepo deviceDimRepo;
    private final JdbcTemplate jdbcTemplate;
    private final ListSqlQuery queries;

    public ClientInfoController(DeviceDimRepo deviceDimRepo, JdbcTemplate jdbcTemplate, @Qualifier("webClientConnectionsQuery") ListSqlQuery queries) {
        this.deviceDimRepo = deviceDimRepo;
        this.jdbcTemplate = jdbcTemplate;
        this.queries = queries;
    }

    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping("/getList.do")
    @Transactional(value = "appJpaTx", readOnly = true)
    public String getClientList(Model model,
                                @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer size) {
        page = resolvePage(page);
        int pageSize = resolvePageSize(size);

        Pageable defaultPage = PageRequest.of(page - 1, pageSize);
        Page<DeviceDimDTO> allCLients = deviceDimRepo.findAll(defaultPage).map(DeviceDimDTO::new);

        model.addAttribute("clients", allCLients);

        return "clientList";
    }

    private static int resolvePage(Integer page) {
        if (page == null) {
            return 1;
        }
        return Math.max(page, 1);
    }

    private static int resolvePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.clamp(size, 1, MAX_PAGE_SIZE);
    }

    @GetMapping("/connection/getInfo.do")
    @Transactional(value = "appTx", readOnly = true)
    public String getClientConnInfo(Model model,
                                    @RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer size,
                                    @RequestParam(required = false) Integer clientKey) {
        page = resolvePage(page);
        int pageSize = resolvePageSize(size);

        // count all results for given client
        Map<String, Object> countAndFilterClientConnInfo = jdbcTemplate.queryForMap(
                queries.getQueryValue("countAndFilterClientConnInfo"),
                clientKey);

        long totalRecords = Long.parseLong(countAndFilterClientConnInfo.get("total_records").toString());
        long[] pages = PaginationUtil.getDefaultPaginationInfo(totalRecords, page, pageSize);

        // fetch all data with given limit
        List<DeviceWlanConnectionsDTO> listConnections = jdbcTemplate.query(
                        queries.getQueryValue("selectAndFilterClientConnInfo"),
                        new BeanPropertyRowMapper<>(DeviceWlanConnectionsFactView.class),
                        clientKey, (page - 1) * pageSize,
                        pageSize)
                .stream()
                .map(DeviceWlanConnectionsDTO::new)
                .toList();

        model.addAttribute("currentPage", page);
        model.addAttribute("pages", pages);
        model.addAttribute("listConnections", listConnections);

        return "clientConnInfo :: connectionsFragment";
    }
}
