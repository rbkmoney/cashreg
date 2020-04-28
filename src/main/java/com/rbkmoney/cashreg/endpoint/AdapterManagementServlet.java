package com.rbkmoney.cashreg.endpoint;

import com.rbkmoney.cashreg.handler.cashreg.CashRegServerManagementHandler;
import com.rbkmoney.damsel.cashreg.processing.ManagementSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/cashreg/management")
public class AdapterManagementServlet extends GenericServlet {

    private final CashRegServerManagementHandler handler;

    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(ManagementSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}

