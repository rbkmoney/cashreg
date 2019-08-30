//package com.rbkmoney.cashreg.scheduled;
//
//import com.github.benmanes.caffeine.cache.Cache;
//import com.rbkmoney.cashreg.entity.*;
//import com.rbkmoney.cashreg.model.Settings;
//import com.rbkmoney.cashreg.model.StatusPolling;
//import com.rbkmoney.cashreg.proto.base.Cash;
//import com.rbkmoney.cashreg.proto.base.ContactInfo;
//import com.rbkmoney.cashreg.proto.base.CurrencyRef;
//import com.rbkmoney.cashreg.proto.provider.*;
//import com.rbkmoney.cashreg.service.CashRegDeliveryService;
//import com.rbkmoney.cashreg.utils.CashRegUtils;
//import com.rbkmoney.cashreg.utils.Converter;
//import com.rbkmoney.cashreg.utils.PageUtils;
//import com.rbkmoney.cashreg.utils.constant.CartState;
//import com.rbkmoney.cashreg.utils.constant.CashRegStatus;
//import com.rbkmoney.cashreg.utils.constant.CashRegTypeOperation;
//import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.util.Assert;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.Optional;
//
//import static com.rbkmoney.cashreg.utils.Converter.extractSettings;
//
//@Slf4j
//@Component
//public class CashregScheduled {
//
//    private final CashRegDeliveryService deliveryService;
//
//    private static final int DEFAULT_PAGE_INDEX = 0;
//
//    private final int queryLimitDebit;
//    private final int queryLimitRefundDebit;
//    private final int queryLimitStatus;
//
//    private final long pollingLimitTimeout;
//    private final int cashRegNetworkTimeout;
//
//    private final Cache<String, StatusPolling> cache;
//
//    @Autowired
//    public CashregScheduled(
//            CashRegDeliveryService deliveryService,
//            @Value("${query.limit.debit:30}") int queryLimitDebit,
//            @Value("${query.limit.refund_debit:30}") int queryLimitRefundDebit,
//            @Value("${query.limit.status:30}") int queryLimitStatus,
//            @Value("${polling.limit.timeout:60}") long pollingLimitTimeout,
//            @Value("${cashreg.networkTimeout:60}") int cashRegNetworkTimeout,
//            Cache<String, StatusPolling> cache
//    ) {
//        Assert.notNull(deliveryService, "deliveryService must not be null");
//
//        this.deliveryService = deliveryService;
//
//        this.queryLimitDebit = queryLimitDebit;
//        this.queryLimitRefundDebit = queryLimitRefundDebit;
//        this.queryLimitStatus = queryLimitStatus;
//
//        this.pollingLimitTimeout = pollingLimitTimeout;
//        this.cashRegNetworkTimeout = cashRegNetworkTimeout;
//
//        this.cache = cache;
//    }
//
//    @Scheduled(fixedRateString = "${cron.expression.debit}")
//    public void debit() {
//
//        int pageLimit = PageUtils.getPageLimit(queryLimitDebit);
//        List<CashRegDelivery> deliveryList = deliveryService.findByTypeOperationAndCashregStatusPagination(
//                CashRegTypeOperation.DEBIT,
//                CashRegStatus.READY,
//                DEFAULT_PAGE_INDEX, pageLimit
//        );
//
//        deliveryList.forEach((delivery) -> {
//
//                    try {
//                        String url = delivery.getInvoiceId().getAccount().getCashbox().getUrl();
//                        CashRegProviderSrv.Iface cashRegProvider = cashRegProviderSrv(url, cashRegNetworkTimeout);
//
//                        CashRegContext context = new CashRegContext();
//                        Settings settings = extractSettings(delivery.getInvoiceId().getAccount().getCashbox().getSettings());
//
//                        context.setAccountInfo(prepareAccountInfo(settings));
//
//                        context.setOptions(settings.prepareMap());
//
//                        PaymentInfo paymentInfo = new PaymentInfo();
//
//                        /**
//                         * if full cart, do full refund debit from invoice cart
//                         * if partial cart, do partial refund debit from refund cart
//                         */
//                        if (delivery.getCartState().equalsIgnoreCase(CartState.FULL)) {
//                            paymentInfo.setCart(Converter.getInvoiceLines(
//                                    delivery.getInvoiceId().getCart(),
//                                    delivery.getInvoiceId().getCurrency()
//                                    )
//                            );
//                        } else {
//                            paymentInfo.setCart(Converter.getInvoiceLines(
//                                    delivery.getRefundId().getCart(),
//                                    delivery.getInvoiceId().getCurrency()
//                                    )
//                            );
//                        }
//
//                        Cash cash = prepareCash(delivery);
//                        paymentInfo.setCash(cash);
//
//                        Payer payer = preparePayer(delivery);
//                        paymentInfo.setPayer(payer);
//
//                        context.setPaymentInfo(paymentInfo);
//
//                        String requestId;
//                        if (delivery.getRequestId() != null && !delivery.getRequestId().isEmpty()) {
//                            requestId = delivery.getRequestId();
//                        } else {
//                            requestId = CashRegUtils.prepareUuid(delivery.getInvoiceId(), delivery.getPaymentId(), delivery.getRefundId(), CashRegTypeOperation.REFUND_DEBIT);
//                        }
//
//                        context.setRequestId(requestId);
//
//                        CashRegResult result = cashRegProvider.debit(context);
//                        delivery.setCashregStatus(CashRegStatus.ERROR);
//
//                        if (result.getIntent().getFinish().getStatus().isSetSuccess()) {
//                            if (result.getIntent().getFinish().getStatus().getSuccess().isSetPayload()) {
//                                delivery.setCashregStatus(CashRegStatus.DONE);
//                            }
//                            if (result.getIntent().getFinish().getStatus().getSuccess().isSetUuid()) {
//                                delivery.setCashregStatus(CashRegStatus.SENT);
//                            }
//                        } else {
//                            delivery.setCashregStatus(CashRegStatus.FAIL);
//                        }
//
//                        delivery.setCashregResponse(result.getOriginalResponse());
//                        delivery.setRequestId(requestId);
//                        deliveryService.save(delivery);
//                    } catch (Exception e) {
//                        String message = prepareMessageUnexpectedError(delivery, CashRegTypeOperation.DEBIT);
//                        log.error(message, e);
//                        delivery.setCashregStatus(CashRegStatus.ERROR);
//                        delivery.setCashregResponse(e.getMessage());
//                        deliveryService.save(delivery);
//                    }
//
//                }
//        );
//
//    }
//
//    @Scheduled(fixedRateString = "${cron.expression.refund_debit}")
//    public void refundDebit() {
//
//        int pageLimit = PageUtils.getPageLimit(queryLimitRefundDebit);
//        List<CashRegDelivery> deliveryList = deliveryService.findByTypeOperationAndCashregStatusPagination(
//                CashRegTypeOperation.REFUND_DEBIT,
//                CashRegStatus.READY,
//                DEFAULT_PAGE_INDEX, pageLimit
//        );
//
//        deliveryList.forEach((delivery) -> {
//
//                    try {
//                        String url = delivery.getInvoiceId().getAccount().getCashbox().getUrl();
//                        CashRegProviderSrv.Iface cashRegProvider = cashRegProviderSrv(url, cashRegNetworkTimeout);
//
//                        // if no refund cart and amount not equals - throw exception
//                        if (delivery.getRefundId() != null &&
//                                !delivery.getInvoiceId().getAmount().equals(delivery.getRefundId().getAmount())
//                                && delivery.getRefundId().getCart() != null
//                        ) {
//                            String message = String.format(CashRegTypeOperation.REFUND_DEBIT + " invoice %s amount != refund %s amount",
//                                    delivery.getInvoiceId(), delivery.getRefundId()
//                            );
//                            log.error(message);
//                            throw new RuntimeException(message);
//                        }
//
//
//                        CashRegContext context = new CashRegContext();
//                        Settings settings = extractSettings(delivery.getInvoiceId().getAccount().getCashbox().getSettings());
//
//                        context.setAccountInfo(prepareAccountInfo(settings));
//                        context.setOptions(settings.prepareMap());
//
//
//                        PaymentInfo paymentInfo = new PaymentInfo();
//
//                        /**
//                         * if full cart, do full refund debit from invoice cart
//                         * if partial cart, do partial refund debit from refund cart
//                         */
//                        if (delivery.getCartState().equalsIgnoreCase(CartState.FULL)) {
//                            paymentInfo.setCart(Converter.getInvoiceLines(
//                                    delivery.getInvoiceId().getCart(),
//                                    delivery.getInvoiceId().getCurrency()
//                                    )
//                            );
//                        } else {
//                            paymentInfo.setCart(Converter.getInvoiceLines(
//                                    delivery.getRefundId().getPreviousCart(),
//                                    delivery.getInvoiceId().getCurrency()
//                                    )
//                            );
//                        }
//
//                        Cash cash = prepareCash(delivery);
//                        paymentInfo.setCash(cash);
//
//                        Payer payer = preparePayer(delivery);
//                        paymentInfo.setPayer(payer);
//
//                        context.setPaymentInfo(paymentInfo);
//
//                        String requestId;
//                        if (delivery.getRequestId() != null && !delivery.getRequestId().isEmpty()) {
//                            requestId = delivery.getRequestId();
//                        } else {
//                            requestId = CashRegUtils.prepareUuid(delivery.getInvoiceId(), delivery.getPaymentId(), delivery.getRefundId(), CashRegTypeOperation.REFUND_DEBIT);
//                        }
//
//                        context.setRequestId(requestId);
//
//                        CashRegResult result = cashRegProvider.refundDebit(context);
//
//                        delivery.setCashregStatus(CashRegStatus.ERROR);
//
//                        if (result.getIntent().getFinish().getStatus().isSetSuccess()) {
//                            if (result.getIntent().getFinish().getStatus().getSuccess().isSetPayload()) {
//                                delivery.setCashregStatus(CashRegStatus.DONE);
//                            }
//                            if (result.getIntent().getFinish().getStatus().getSuccess().isSetUuid()) {
//                                delivery.setCashregStatus(CashRegStatus.SENT);
//                            }
//                        } else {
//                            delivery.setCashregStatus(CashRegStatus.FAIL);
//                        }
//
//                        delivery.setCashregResponse(result.getOriginalResponse());
//                        delivery.setRequestId(requestId);
//                        deliveryService.save(delivery);
//                    } catch (Exception e) {
//                        String message = prepareMessageUnexpectedError(delivery, CashRegTypeOperation.REFUND_DEBIT);
//                        log.error(message, e);
//                        delivery.setCashregStatus(CashRegStatus.ERROR);
//                        delivery.setCashregResponse(e.getMessage());
//                        deliveryService.save(delivery);
//                    }
//
//                }
//        );
//    }
//
//
//    @Scheduled(fixedRateString = "${cron.expression.status}")
//    public void status() {
//
//        int pageLimit = PageUtils.getPageLimit(queryLimitStatus);
//        List<CashRegDelivery> deliveryList = deliveryService.findByCashregStatusPagination(
//                CashRegStatus.SENT, DEFAULT_PAGE_INDEX, pageLimit
//        );
//
//        deliveryList.forEach((delivery) -> {
//
//                    String url = delivery.getInvoiceId().getAccount().getCashbox().getUrl();
//                    CashRegProviderSrv.Iface kktProvider = cashRegProviderSrv(url, cashRegNetworkTimeout);
//
//                    CashRegContext context = new CashRegContext();
//                    Settings settings = extractSettings(delivery.getInvoiceId().getAccount().getCashbox().getSettings());
//
//                    context.setAccountInfo(prepareAccountInfo(settings));
//
//                    context.setOptions(settings.prepareMap());
//                    context.setRequestId(delivery.getCashregUuid());
//
//                    PaymentInfo paymentInfo = new PaymentInfo();
//                    paymentInfo.setCart(Converter.getInvoiceLines(
//                            delivery.getInvoiceId().getExchangeCart(),
//                            delivery.getInvoiceId().getCurrency())
//                    );
//
//                    Cash cash = prepareCash(delivery);
//                    paymentInfo.setCash(cash);
//
//                    Payer payer = preparePayer(delivery);
//                    paymentInfo.setPayer(payer);
//
//                    context.setPaymentInfo(paymentInfo);
//
//                    try {
//                        CashRegResult result = kktProvider.getStatus(context);
//                        checkTimeoutPolling(delivery, cache, pollingLimitTimeout);
//
//                        if (result.getIntent().getFinish().getStatus().isSetSuccess()) {
//                            if (result.getIntent().getFinish().getStatus().getSuccess().isSetPayload()) {
//                                delivery.setCashregStatus(CashRegStatus.DONE);
//                                removeKeyFromCache(delivery.getRequestId(), cache);
//                            }
//                        } else {
//                            delivery.setCashregStatus(CashRegStatus.FAIL);
//                            removeKeyFromCache(delivery.getRequestId(), cache);
//                        }
//
//                        delivery.setCashregResponse(result.getOriginalResponse());
//                        deliveryService.save(delivery);
//                    } catch (Exception e) {
//                        String message = prepareMessageUnexpectedError(delivery, "status");
//                        log.error(message, e);
//                        delivery.setCashregStatus(CashRegStatus.ERROR);
//                        delivery.setCashregResponse(e.getMessage());
//                        deliveryService.save(delivery);
//                    }
//
//                }
//        );
//
//    }
//
//    private Payer preparePayer(CashRegDelivery delivery) {
//        Payer payer = new Payer();
//        ContactInfo contactInfo = new ContactInfo();
//        if (delivery.getPaymentId().getPayerInfo().getContactType().getType().equalsIgnoreCase(ContactType.EMAIL)) {
//            contactInfo.setEmail(delivery.getPaymentId().getPayerInfo().getContact());
//        } else {
//            contactInfo.setPhoneNumber(delivery.getPaymentId().getPayerInfo().getContact());
//        }
//        payer.setContactInfo(contactInfo);
//        return payer;
//    }
//
//    private Cash prepareCash(CashRegDelivery delivery) {
//        CurrencyRef currencyRef = new CurrencyRef();
//        currencyRef.setSymbolicCode(delivery.getInvoiceId().getCurrency());
//
//        Cash cash = new Cash();
//        cash.setAmount(delivery.getInvoiceId().getAmount());
//        cash.setCurrency(currencyRef);
//        return cash;
//    }
//
//    private AccountInfo prepareAccountInfo(Settings settings) {
//        AccountInfo accountInfo = new AccountInfo();
//        CompanyInfo companyInfo = new CompanyInfo();
//        companyInfo.setAddress(settings.getCompanyAddress());
//        companyInfo.setEmail(settings.getCompanyEmail());
//        companyInfo.setInn(settings.getInn());
//        companyInfo.setName(settings.getCompanyName());
//        companyInfo.setTaxMode(prepareTaxMode(settings.getTaxMode()));
//        accountInfo.setCompanyInfo(companyInfo);
//        return accountInfo;
//    }
//
//    private void checkTimeoutPolling(CashRegDelivery delivery, Cache<String, StatusPolling> cache, long pollingLimitTimeout) {
//
//        String requestId = delivery.getRequestId();
//        StatusPolling statusPolling = cache.getIfPresent(requestId);
//
//        Instant currentTime = Instant.now();
//        if (statusPolling == null) {
//
//            Instant maxDateTime = currentTime.plus(pollingLimitTimeout, ChronoUnit.MINUTES);
//            statusPolling = new StatusPolling();
//            statusPolling.setStartDateTimePolling(currentTime);
//            statusPolling.setMaxDateTimePolling(maxDateTime);
//
//            cache.put(requestId, statusPolling);
//            log.debug("Cache: put - requestId {}, statusPolling {}, cache size {}", requestId, statusPolling, cache.estimatedSize());
//        } else {
//            if (statusPolling.timeIsOver(currentTime)) {
//                delivery.setCashregStatus(CashRegStatus.TIMEOUT);
//                log.debug("Cache: timeout - requestId {}, statusPolling {}, cache size {}", requestId, statusPolling, cache.estimatedSize());
//                cache.invalidate(requestId);
//            }
//        }
//    }
//
//    private void removeKeyFromCache(String requestId, Cache<String, StatusPolling> cache) {
//        StatusPolling statusPolling = cache.getIfPresent(requestId);
//        if (statusPolling != null) {
//            log.debug("Cache: remove key - requestId {}, statusPolling {}, cache size {}", requestId, statusPolling, cache.estimatedSize());
//            cache.invalidate(requestId);
//        }
//    }
//
//    private String prepareMessageUnexpectedError(CashRegDelivery cashRegDelivery, String cashRegTypeOperation) {
//
//        String cashboxName = Optional.ofNullable(cashRegDelivery).map(CashRegDelivery::getInvoiceId).map(InvoicePayer::getAccount).map(Account::getCashbox).map(CashBox::getName).orElse("");
//        String invoiceId = Optional.ofNullable(cashRegDelivery).map(CashRegDelivery::getInvoiceId).map(InvoicePayer::getInvoiceId).orElse("");
//        String paymentId = Optional.ofNullable(cashRegDelivery).map(CashRegDelivery::getPaymentId).map(Payment::getPaymentId).orElse("");
//        String refundId = Optional.ofNullable(cashRegDelivery).map(CashRegDelivery::getRefundId).map(Refund::getRefundId).orElse("");
//
//        // Unexpected error atol debit with invoice_id zazjXms3bE, payment_id 1, refund_id 1
//        return String.format("Unexpected error %s %s with invoice_id %s, payment_id %s, refund_id %s",
//                cashboxName, cashRegTypeOperation, invoiceId, paymentId, refundId
//        );
//    }
//
//    private static TaxMode prepareTaxMode(String taxMode) {
//        return TaxMode.valueOf(taxMode);
//    }
//
//    public CashRegProviderSrv.Iface cashRegProviderSrv(String url, Integer networkTimeout) {
//        try {
//            return new THSpawnClientBuilder()
//                    .withAddress(new URI(url))
//                    .withNetworkTimeout(networkTimeout)
//                    .build(CashRegProviderSrv.Iface.class);
//        } catch (URISyntaxException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//}
