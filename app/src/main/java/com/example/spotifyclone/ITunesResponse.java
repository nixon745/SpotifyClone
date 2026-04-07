package com.example.spotifyclone;

import java.util.List;

public class ITunesResponse {
    // השרת של iTunes מחזיר אובייקט שיש בתוכו רשימה שנקראת results
    private List<ITunesResult> results;

    public List<ITunesResult> getResults() {
        return results;
    }
}