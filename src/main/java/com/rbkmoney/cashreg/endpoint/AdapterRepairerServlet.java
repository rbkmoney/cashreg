package com.rbkmoney.cashreg.endpoint;

import com.rbkmoney.cashreg.handler.cashreg.CashRegServerRepairerHandler;
import com.rbkmoney.damsel.cashreg_processing.RepairerSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/cashreg/repairer")
public class AdapterRepairerServlet extends GenericServlet {

    private final CashRegServerRepairerHandler handler;

    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(RepairerSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}

