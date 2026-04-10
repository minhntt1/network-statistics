package com.home.network.statistic.admin.web;

import com.home.network.statistic.poller.aruba.iap.in.ArubaSnmpAiTarget;
import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.igate.gw240.in.IngestionCredentials;
import com.home.network.statistic.poller.rfc1213.in.SnmpTarget;
import com.home.network.statistic.poller.tplink.deco.in.WebUiCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.home.network.statistic.common.util.PaginationUtil;
import com.home.network.statistic.poller.authentication.AuthDataService;

import lombok.RequiredArgsConstructor;

@Slf4j
@Controller
@RequestMapping("/web/modem/auth")
@RequiredArgsConstructor
@Profile({"dev-admin", "prd-admin"})
public class ModemAuthController {
	private static final int LIMIT_LIST_RESULT = 20;
	private final AuthDataService authDataService;

	static {
		// init valid classes to display on UI form
		new WebUiCredentials();
		new IngestionCredentials();
		new SnmpTarget();
		new ArubaSnmpAiTarget();
	}

	@GetMapping("/getListAuthInfo.do")
	@Transactional(value = "appJpaTx", readOnly = true)
	public String getListAuthInfo(Model model, 
			@RequestParam(required = false) Integer page,
		  	@RequestParam(required = false) Integer updateId,
		  	@RequestParam(required = false) Boolean result) {
		page = page == null ? 1 : page;
		
		var cntResult = authDataService.countResultAll();
		var pagination = PaginationUtil.getDefaultPaginationInfo(cntResult, page, LIMIT_LIST_RESULT);
		var list = authDataService.findAll(page, LIMIT_LIST_RESULT);
		
		model.addAttribute("listAuthInfo", list);
		model.addAttribute("pagination", pagination);
		model.addAttribute("currPage", page);
		model.addAttribute("result", result);
		model.addAttribute("authDataUpdate", authDataService.findById(updateId));
		model.addAttribute("templateDataClass", AuthData.extractClassAndJsonTemplate());

		return "modemAuth";
	}

	@PostMapping("/postModemAuthInfo.do")
	public String postModemAuthInfo(@ModelAttribute AuthData data) {
		if (data.checkValidData()) {
			authDataService.upsertAuthData(data);
			return "redirect:/web/modem/auth/getListAuthInfo.do?result=true";
		}

		return "redirect:/web/modem/auth/getListAuthInfo.do?result=false";
	}

	@PostMapping("/deleteModemAuthInfo.do")
	public String deleteModemAuthInfo(@RequestParam Integer id) {
		if (authDataService.deleteById(id)) {
			return "redirect:/web/modem/auth/getListAuthInfo.do?result=true";
		}

		return "redirect:/web/modem/auth/getListAuthInfo.do?result=false";
	}
}
