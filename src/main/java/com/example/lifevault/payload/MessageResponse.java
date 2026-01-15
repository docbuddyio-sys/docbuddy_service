package com.example.lifevault.payload;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data
//@AllArgsConstructor
public class MessageResponse {
    private String message;

     public MessageResponse(String message) {
     this.message = message;
     }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
