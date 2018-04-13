package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.SameAccountIdException;
import com.db.awmd.challenge.service.EmailNotificationService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

@Override
public void instantTransfer(Transfer transfer) throws SameAccountIdException, InsufficientFundsException {
	// TODO Auto-generated method stub
	
	EmailNotificationService notify = new EmailNotificationService();
	
	String toAccount = transfer.getToAccountId();
	String fromAccount = transfer.getFromAccountId();
	BigDecimal amount = transfer.getAmount();
	
	if(toAccount.equals(fromAccount)){
		throw new SameAccountIdException("Provided 'To' and 'From' accounts are same");
	}
	
	Account toAccountDetails = accounts.get(toAccount);
	Account fromAccountDetails = accounts.get(fromAccount);
	
	if(amount.compareTo(fromAccountDetails.getBalance()) > 0){
		throw new InsufficientFundsException("This account does not have sufficient fund to transfer");
	}
	
	System.out.println("Updated toAccount detaiils "+(toAccountDetails.getBalance()).subtract(amount));
	toAccountDetails.setBalance((toAccountDetails.getBalance()).add(amount));
	String messageto = "Updated balance is: "+toAccountDetails.getBalance();

	notify.notifyAboutTransfer(toAccountDetails, messageto);

	System.out.println("Updated fromAccount detaiils: "+(fromAccountDetails.getBalance()).add(amount));
	fromAccountDetails.setBalance((fromAccountDetails.getBalance()).subtract(amount));
	String messagefrom = "Updated balance is: "+fromAccountDetails.getBalance();
	
	notify.notifyAboutTransfer(fromAccountDetails, messagefrom);

}

}
