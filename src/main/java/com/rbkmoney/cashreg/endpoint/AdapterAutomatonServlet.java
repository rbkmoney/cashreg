package com.rbkmoney.cashreg.endpoint;

import com.rbkmoney.cashreg.handler.machinegun.ManagementProcessorHandler;
import com.rbkmoney.machinegun.stateproc.ProcessorSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/v1/processor")
public class AdapterAutomatonServlet extends GenericServlet {

    private final ManagementProcessorHandler handler;

    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(ProcessorSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}

