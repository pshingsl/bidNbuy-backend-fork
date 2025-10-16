package com.bidnbuy.server.controller;

import com.bidnbuy.server.config.TossPaymentClient;
import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.PaymentEntity;
import com.bidnbuy.server.exception.PaymentErrorResponse;
import com.bidnbuy.server.service.OrderService;
import com.bidnbuy.server.service.PaymentService;
import com.sun.tools.jconsole.JConsoleContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RequestMapping("/payments")
@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * 결제 준비 (PENDING 저장)
     */
    @PostMapping("/saveAmount")
    public ResponseEntity<?> saveAmount(@RequestBody SaveAmountRequest request) {
        try {
            // OrderEntity 조회
            OrderEntity order = orderService.findById(request.getOrderId());

            // PaymentEntity 생성 (order FK 연결 필수)
            PaymentEntity payment = paymentService.createPendingPayment(order, request);

            log.info("✅ Payment pending saved: merchantOrderId={}, amount={}",
                    payment.getMerchantOrderId(), payment.getTotalAmount());

            return ResponseEntity.ok(new PaymentPendingResponseDto(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    PaymentErrorResponse.builder()
                            .code(400)
                            .message("결제 준비 중 오류: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * 결제 승인 처리
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentRequestDTO request) {
        try {
            log.info("Confirm 요청: paymentKey={}, orderId={}, amount={}",
                    request.getPaymentKey(), request.getOrderId(), request.getAmount());

            // 1) Toss 승인 요청 : paymentKey, orderId(mercharId), amount만
            HttpResponse<String> response = tossPaymentClient.requestConfirm(request);

            if (response.statusCode() != 200) {
                return ResponseEntity.status(response.statusCode()).body(response.body());
            }

            // 2) 응답 파싱
            PaymentResponseDto dto = tossPaymentClient.parseConfirmResponse(response.body());

            log.info("✅ 승인 성공: {}",
                    dto);

            // 3) DB 갱신
            PaymentEntity payment = paymentService.saveConfirmedPayment(dto);

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    PaymentErrorResponse.builder()
                            .code(500)
                            .message("결제 승인 처리 중 오류 발생: " + e.getMessage())
                            .build()
            );
        }
    }

    //  사용자 취소 요청 (전액 취소)
    @PostMapping("/cancel")
    public ResponseEntity<PaymentCancelResponseDto> cancelPayment(@RequestBody PaymentCancelRequestDto requestDto) {
        PaymentCancelResponseDto result = paymentService.cancelPayment(requestDto);
        return ResponseEntity.ok(result);
    }


}


//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private static final String WIDGET_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
//    private static final String API_SECRET_KEY = "test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R";
//    private final Map<String, String> billingKeyMap = new HashMap<>();
//
//    @RequestMapping(value = {"/confirm/widget", "/confirm/payment"})
//    public ResponseEntity<JSONObject> confirmPayment(HttpServletRequest request, @RequestBody String jsonBody) throws Exception {
//        String secretKey = request.getRequestURI().contains("/confirm/payment") ? API_SECRET_KEY : WIDGET_SECRET_KEY;
//        JSONObject response = sendRequest(parseRequestData(jsonBody), secretKey, "https://api.tosspayments.com/v1/payments/confirm");
//        int statusCode = response.containsKey("error") ? 400 : 200;
//        return ResponseEntity.status(statusCode).body(response);
//    }
//
//    @RequestMapping(value = "/confirm-billing")
//    public ResponseEntity<JSONObject> confirmBilling(@RequestBody String jsonBody) throws Exception {
//        JSONObject requestData = parseRequestData(jsonBody);
//        String billingKey = billingKeyMap.get(requestData.get("customerKey"));
//        JSONObject response = sendRequest(requestData, API_SECRET_KEY, "https://api.tosspayments.com/v1/billing/" + billingKey);
//        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
//    }
//
//    @RequestMapping(value = "/issue-billing-key")
//    public ResponseEntity<JSONObject> issueBillingKey(@RequestBody String jsonBody) throws Exception {
//        JSONObject requestData = parseRequestData(jsonBody);
//        JSONObject response = sendRequest(requestData, API_SECRET_KEY, "https://api.tosspayments.com/v1/billing/authorizations/issue");
//
//        if (!response.containsKey("error")) {
//            billingKeyMap.put((String) requestData.get("customerKey"), (String) response.get("billingKey"));
//        }
//
//        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
//    }
//
//    @RequestMapping(value = "/callback-auth", method = RequestMethod.GET)
//    public ResponseEntity<JSONObject> callbackAuth(@RequestParam String customerKey, @RequestParam String code) throws Exception {
//        JSONObject requestData = new JSONObject();
//        requestData.put("grantType", "AuthorizationCode");
//        requestData.put("customerKey", customerKey);
//        requestData.put("code", code);
//
//        String url = "https://api.tosspayments.com/v1/brandpay/authorizations/access-token";
//        JSONObject response = sendRequest(requestData, API_SECRET_KEY, url);
//
//        logger.info("Response Data: {}", response);
//
//        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
//    }
//
//    @RequestMapping(value = "/confirm/brandpay", method = RequestMethod.POST, consumes = "application/json")
//    public ResponseEntity<JSONObject> confirmBrandpay(@RequestBody String jsonBody) throws Exception {
//        JSONObject requestData = parseRequestData(jsonBody);
//        String url = "https://api.tosspayments.com/v1/brandpay/payments/confirm";
//        JSONObject response = sendRequest(requestData, API_SECRET_KEY, url);
//        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
//    }
//
//    private JSONObject parseRequestData(String jsonBody) {
//        try {
//            return (JSONObject) new JSONParser().parse(jsonBody);
//        } catch (ParseException e) {
//            logger.error("JSON Parsing Error", e);
//            return new JSONObject();
//        }
//    }
//
//    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
//        HttpURLConnection connection = createConnection(secretKey, urlString);
//        try (OutputStream os = connection.getOutputStream()) {
//            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
//        }
//
//        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
//             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
//            return (JSONObject) new JSONParser().parse(reader);
//        } catch (Exception e) {
//            logger.error("Error reading response", e);
//            JSONObject errorResponse = new JSONObject();
//            errorResponse.put("error", "Error reading response");
//            return errorResponse;
//        }
//    }
//
//    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
//        URL url = new URL(urlString);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestMethod("POST");
//        connection.setDoOutput(true);
//        return connection;
//    }
//
//    @RequestMapping(value = "/", method = RequestMethod.GET)
//    public String index() {
//        return "/widget/checkout";
//    }
//
//    @RequestMapping(value = "/fail", method = RequestMethod.GET)
//    public String failPayment(HttpServletRequest request, Model model) {
//        model.addAttribute("code", request.getParameter("code"));
//        model.addAttribute("message", request.getParameter("message"));
//        return "/fail";
//    }

