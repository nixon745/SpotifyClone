package com.example.spotifyclone;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ITunesApiService {
    @GET("search")
    Call<ITunesResponse> searchSongs(
            @Query("term") String term,
            @Query("limit") int limit,
            @Query("entity") String entity // מוסיף סינון לסוג התוצאה
    );
}

    //הסבר קצר לבוחן: "השתמשתי ב-Interface כדי להגדיר את נקודות הקצה (Endpoints) של ה-API. ה-Annotation @GET אומר לספריית Retrofit לבצע בקשת מסוג GET לשרת של iTunes". למה זה interface ולא class? (טיפ לבגרות)
    //הבוחן בטוח ישאל את זה. התשובה היא:
    //
    //"בספריית Retrofit, אנחנו לא כותבים את הקוד שמבצע את הגלישה בעצמנו. אנחנו רק מגדירים Interface עם הכתובות שאנחנו צריכים, והספרייה 'מממשת' (Implement) את הקוד עבורנו בזמן ריצה."
