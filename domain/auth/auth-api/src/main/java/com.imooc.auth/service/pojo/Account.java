package com.imooc.auth.service.pojo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {
  private String userId;
  private String token;
  private String refreshToken;
  private boolean skipVerification = false;
}
