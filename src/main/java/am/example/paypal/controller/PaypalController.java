package am.example.paypal.controller;

import am.example.paypal.model.PaypalPaymentModel;
import am.example.paypal.paypal.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {

    @Value("${app.cancelUrl}")
    public String cancelUrl;

    @Value("${app.successUrl}")
    public String successUrl;

    private final PaypalService paypalService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment(
            @RequestParam("method") String method,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description
    ) {
        PaypalPaymentModel ppm = PaypalPaymentModel
                .builder()
                .total(amount)
                .currency(currency)
                .method(method)
                .intent("sale")
                .description(description)
                .cancelUrl(cancelUrl)
                .successUrl(successUrl)
                .build();
        Payment payment = paypalService.createPayment(ppm);
        for (Links link : payment.getLinks()) {
            if (link.getRel().equals("approval_url"))
                return new RedirectView(link.getHref());
        }
        return new RedirectView("/payment/error");
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId,
                                 @RequestParam("PayerID") String payerId) {
        Payment payment = paypalService.executePayment(paymentId, payerId);
        if (payment.getState().equals("approved")) {
            return "paymentSuccess";
        }
        return "paymentSuccess";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "paymentCancel";
    }

    @GetMapping("/payment/error")
    public String paymentError() {
        return "paymentError";
    }
}
