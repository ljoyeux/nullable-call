package fr.devlogic.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NullableCallTest {
    @Test
    public void list() {
        List<String> list = NullableCall.nullableCall(List.class, null);

        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    public void accounts() {
        User user = new User();
        user.setLogin("login");
        user.setPassword("password");

        try {
            System.out.println(user.getAccounts().size());
            Assertions.fail();
        } catch (NullPointerException ex) {
            // ok
        }

        User proxyUser = NullableCall.nullableCall(user);

        Assertions.assertEquals(0, proxyUser.getAccounts().size());

        // add Accounts through proxy
        Assertions.assertNotNull(proxyUser.getAccounts());
        Assertions.assertTrue(proxyUser.getAccounts().isEmpty());
        Assertions.assertNull(user.getAccounts());

        proxyUser.setAccounts(new ArrayList<>());

        Assertions.assertNotNull(user.getAccounts());

        // accounts
        List<Account> accounts = new ArrayList<>();
        Account account = new Account();
        accounts.add(account);
        user.setAccounts(accounts);

        //
        Assertions.assertEquals(1, proxyUser.getAccounts().size());

        // add Accounts through non proxy
        user.setAccounts(null);
        Assertions.assertNotNull(proxyUser.getAccounts());
        Assertions.assertEquals(0, proxyUser.getAccounts().size());

        user.setAccounts(accounts);

        Assertions.assertEquals(1, user.getAccounts().size());

        Assertions.assertEquals(1, proxyUser.getAccounts().size());
    }
}
