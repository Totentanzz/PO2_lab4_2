package app.web;

import retrofit2.Call;
import retrofit2.http.*;

public interface FigureAPI {
    @POST("/register")
    Call<String> registerClient();

    @POST("/figures/{clientName}")
    Call<Void> createOrReplaceShape(@Path("clientName") String clientName, @Body String xml);

    @GET("/figures/{clientName}")
    Call<String> getShape(@Path("clientName") String clientName);

    @DELETE("/figures/{clientName}")
    Call<Void> deleteClient(@Path("clientName") String clientName);
}
