package ua.solvd.gorest;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeMethod;
import tools.jackson.databind.ObjectMapper;
import ua.solvd.gorest.constant.Constant;
import ua.solvd.gorest.model.ApiResponse;
import ua.solvd.gorest.model.UserPayload;
import ua.solvd.gorest.service.UsersApiService;
import ua.solvd.gorest.util.PayloadTemplate;
import ua.solvd.gorest.util.TokenUtil;

public class BaseTest {
    protected RequestSpecification authRequestSpec;
    protected RequestSpecification unAuthRequestSpec;
    protected ObjectMapper mapper;
    protected UsersApiService apiService;

    @BeforeMethod
    public void setupConfig() {
        RestAssured.baseURI = Constant.AUTH_URL;
        mapper = new ObjectMapper();
        String token = TokenUtil.getTokenFromProperties();
        authRequestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader(Constant.HEADER_AUTHORIZATION,
                        Constant.HEADER_AUTHORIZATION_BEARER + token)
                .build();
        unAuthRequestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
        apiService = new UsersApiService(authRequestSpec, unAuthRequestSpec);
    }

    private ApiResponse<UserPayload> createTemporaryUser() {
        UserPayload payload = PayloadTemplate.getValidUser();
        return apiService.createUser(payload);
    }

    protected int getTemporaryUserId() {
        return createTemporaryUser().getBody().id();
    }
}