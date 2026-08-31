package com.cesi_zen_back.cesi_zen_back.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.DefaultCsrfToken;

class CsrfControllerTest {

  @Test
  void csrf_shouldReturnCsrfToken() {
    CsrfController controller = new CsrfController();

    DefaultCsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "csrf-token-value");

    Map<String, String> response = controller.csrf(csrfToken);

    assertThat(response).containsEntry("token", "csrf-token-value");
  }
}
