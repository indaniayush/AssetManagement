package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  
  /*Instant Transfer Test Cases: 10 scenarios identified*/
  
  /*Case 1. Success scenario*/
  
  @Test
  public void instantTransfer() throws Exception {
 
  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"db001\",\"balance\":1000}")).andExpect(status().isCreated());
  
  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"db123\",\"balance\":800}")).andExpect(status().isCreated());
	  
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"toAccountId\":\"db001\",\"fromAccountId\":\"db123\",\"amount\":100.50}")).andExpect(status().isCreated());

    Account toAccount = accountsService.getAccount("db001");
    Account fromAccount = accountsService.getAccount("db123");
    
    assertThat(toAccount.getAccountId()).isEqualTo("db001");
    assertThat(toAccount.getBalance()).isEqualByComparingTo("1100.50");
    
    assertThat(fromAccount.getAccountId()).isEqualTo("db123");
    assertThat(fromAccount.getBalance()).isEqualByComparingTo("699.50");
    
  }
  
  /*Case 2. If balance in 'fromAccount' is < 'amount' to be transfered*/ 
  
  @Test
  public void insufficientFundsInFromAccountId() throws Exception {
 
  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"db001\",\"balance\":1000}")).andExpect(status().isCreated());
  
  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"db123\",\"balance\":400}")).andExpect(status().isCreated());
	  
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"toAccountId\":\"db001\",\"fromAccountId\":\"db123\",\"amount\":600.50}")).andExpect(status().isBadRequest());
    
  }
  
  /*Case 3. If 'to' and 'from' accounts are same*/
  
  @Test
  public void instantTransferToSameAccount() throws Exception {
 
  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"db001\",\"balance\":1000}")).andExpect(status().isCreated());
	  
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"toAccountId\":\"db001\",\"fromAccountId\":\"db001\",\"amount\":100.50}")).andExpect(status().isBadRequest());

  }
  
  /*Case 4. If 'amount' to be transfered is 'NULL'*/
  
  @Test
  public void instantTransferWithAmountNull() throws Exception {
 
  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"db001\",\"balance\":1000}")).andExpect(status().isCreated());
  
  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	      .content("{\"accountId\":\"db123\",\"balance\":800}")).andExpect(status().isCreated());
	  
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"toAccountId\":\"db001\",\"fromAccountId\":\"db123\",\"amount\":}")).andExpect(status().isBadRequest());
  }
  
  /*Case 5. If request body is empty*/ 
  
  @Test
  public void instantTransferWithNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
  }
  
  /*Case 6. If 'amount' to be transfered is negative*/
  
  @Test
  public void instantTransferWithNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON).content("{\"toAccountId\":\"db001\",\"fromAccountId\":\"db123\",\"amount\":-109.90}")).andExpect(status().isBadRequest());
  }
  
  /*Case 7. If 'toAccountId' is not provided in the request*/
  
  @Test
  public void toAccountIdMissing() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON).content("{\"fromAccountId\":\"db123\",\"amount\":1000}")).andExpect(status().isBadRequest());
  }
  
  /*Case 8. If 'fromAccountId' is not provided in the request*/
  
  @Test
  public void fromAccountIdMissing() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON).content("{\"toAccountId\":\"db123\",\"amount\":1000}")).andExpect(status().isBadRequest());
  }
  
  /*Case 9. If 'amount' is not provided in the request*/
  
  @Test
  public void amountMissing() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/instantTransfer").contentType(MediaType.APPLICATION_JSON).content("{\"toAccountId\":\"db123\",\"fromAccountId\":\"db123\"}")).andExpect(status().isBadRequest());
  }
  
}
