package org.openmrs.module.reporting.web.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.report.Report;
import org.openmrs.module.report.ReportRequest;
import org.openmrs.module.report.renderer.RenderingMode;
import org.openmrs.module.report.service.ReportService;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.web.renderers.WebReportRenderer;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ReportHistoryController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping("/module/reporting/reports/reportHistory")
	public void showReportHistory(ModelMap model) {
		List<ReportRequest> complete = new ArrayList<ReportRequest>(Context.getService(ReportService.class)
		        .getCompletedReportRequests());
		List<ReportRequest> queue = new ArrayList<ReportRequest>(Context.getService(ReportService.class)
		        .getQueuedReportRequests());
		Collections.reverse(complete);
		Collections.reverse(queue);
		model.addAttribute("complete", complete);
		model.addAttribute("queue", queue);
		
		Map<ReportRequest, String> shortNames = new HashMap<ReportRequest, String>();
		Map<ReportRequest, Boolean> isWebRenderer = new HashMap<ReportRequest, Boolean>();
		for (ReportRequest r : complete) {
			if (r.getRenderingMode().getRenderer() instanceof WebReportRenderer) {
				shortNames.put(r, "Web");
				isWebRenderer.put(r, true);
			} else {
				String filename = r.getRenderingMode().getRenderer().getFilename(r.getReportDefinition(),
				    r.getRenderingMode().getArgument());
				try {
					filename = filename.substring(filename.lastIndexOf('.') + 1);
					filename = filename.toUpperCase();
				}
				catch (Exception ex) {}
				shortNames.put(r, filename);
				isWebRenderer.put(r, false);
			}
		}
		model.addAttribute("shortNames", shortNames);
		model.addAttribute("isWebRenderer", isWebRenderer);
	}
	
	@RequestMapping("/module/reporting/reports/reportHistoryDelete")
	public String deleteFromHistory(@RequestParam("uuid") String uuid) {
		Context.getService(ReportService.class).deleteFromHistory(uuid);
		return "redirect:reportHistory.form";
	}
	
	@RequestMapping("/module/reporting/reports/reportHistorySave")
	public String saveHistoryElement(@RequestParam("uuid") String uuid) {
		ReportService service = Context.getService(ReportService.class);
		ReportRequest req = service.getReportRequestByUuid(uuid);
		if (req != null)
			service.archiveReportRequest(req);
		return "redirect:reportHistory.form";
	}
	
	@RequestMapping("/module/reporting/reports/reportHistoryAddLabel")
	public String addLabel(@RequestParam("uuid") String uuid, @RequestParam("label") String label) {
		ReportService service = Context.getService(ReportService.class);
		ReportRequest req = service.getReportRequestByUuid(uuid);
		req.addLabel(label);
		service.saveReportRequest(req);
		return "redirect:reportHistory.form";
	}
	
	@RequestMapping("/module/reporting/reports/reportHistoryRemoveLabel")
	public String removeLabel(@RequestParam("uuid") String uuid, @RequestParam("label") String label) {
		ReportService service = Context.getService(ReportService.class);
		ReportRequest req = service.getReportRequestByUuid(uuid);
		req.removeLabel(label);
		service.saveReportRequest(req);
		return "redirect:reportHistory.form";
	}
	
	@RequestMapping("/module/reporting/reports/reportHistoryOpen")
	public ModelAndView openFromHistory(@RequestParam("uuid") String uuid,
	                            HttpServletResponse response,
	                            WebRequest request,
	                            ModelMap model) throws IOException {
		try {
			Report report = Context.getService(ReportService.class).getReportByUuid(uuid);
			if (report.getRequest().getRenderingMode().getRenderer() instanceof WebReportRenderer) {
				RenderingMode rm = report.getRequest().getRenderingMode();
				request.setAttribute(ReportingConstants.OPENMRS_REPORT_DATA, report.getRawData(), WebRequest.SCOPE_SESSION);
				request.setAttribute(ReportingConstants.OPENMRS_REPORT_ARGUMENT, rm.getArgument(), WebRequest.SCOPE_SESSION);
				
				String url = ((WebReportRenderer) rm.getRenderer()).getLinkUrl(report.getRequest().getReportDefinition());
				if (!url.startsWith("/"))
					url = "/" + url;
				url = request.getContextPath() + url;
				request.setAttribute(ReportingConstants.OPENMRS_LAST_REPORT_URL, url, WebRequest.SCOPE_SESSION);
				return new ModelAndView(new RedirectView(url));
			} else {
				model.addAttribute("report", report);
				return new ModelAndView("/module/reporting/reports/reportHistoryOpen", model);
			}
		} catch (APIException ex) {
			if (ex.getMessage().startsWith("The persisted Report file is missing")) {
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The saved file is still being written. Try again in a few minutes", WebRequest.SCOPE_SESSION);
			} else {
				log.error("Unexpected exception", ex);
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, ex.getMessage(), WebRequest.SCOPE_SESSION);
			}
			return new ModelAndView(new RedirectView("/module/reporting/reports/reportHistory.form"));
		}
	}
	
	@RequestMapping("/module/reporting/reports/reportHistoryDownload")
	public void downloadFromHistory(@RequestParam("uuid") String uuid,
	                            HttpServletResponse response,
	                            WebRequest request) throws IOException {
		Report report = Context.getService(ReportService.class).getReportByUuid(uuid);
		if (report.getRenderedFilename() == null) {
			throw new RuntimeException("This report doesn't have a rendered filename");
		}
		String filename = report.getRenderedFilename().replace(" ", "_");
		response.setContentType(report.getRenderedContentType());
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		response.setHeader("Pragma", "no-cache");
		response.getOutputStream().write(report.getRenderedOutput());
	}
}