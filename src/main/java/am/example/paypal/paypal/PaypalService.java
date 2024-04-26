package am.example.paypal.paypal;

import am.example.paypal.model.PaypalPaymentModel;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaypalService {

    private final APIContext apiContext;

    public Payment createPayment(PaypalPaymentModel paypalPaymentModel) {
        try {
            Amount amount = new Amount();
            amount.setCurrency(paypalPaymentModel.getCurrency());
            amount.setTotal(String.format(Locale.forLanguageTag(paypalPaymentModel.getCurrency()), "%.2f", paypalPaymentModel.getTotal()));

            Transaction transaction = new Transaction();
            transaction.setDescription(paypalPaymentModel.getDescription());
            transaction.setAmount(amount);

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod(paypalPaymentModel.getMethod());

            Payment payment = new Payment();
            payment.setIntent(paypalPaymentModel.getIntent());
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(paypalPaymentModel.getCancelUrl());
            redirectUrls.setReturnUrl(paypalPaymentModel.getSuccessUrl());
            payment.setRedirectUrls(redirectUrls);
            return payment.create(apiContext);
        } catch (PayPalRESTException e) {
            log.error("Payment fails");
            return null;
        }
    }

    public Payment executePayment(String paymentId, String payerId) {
        try {
            Payment payment = new Payment();
            payment.setId(paymentId);
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);
            return payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException e) {
            log.error("Payment fails");
            return null;
        }
    }
}