package PPC.controller;

import PPC.database.PPCDatabase;
import PPC.database.PPCDatabaseManager;
import PPC.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

@Controller
public class AuthenticationHandler {

    private final PPCDatabaseManager dbManager;

    public AuthenticationHandler() throws SQLException, ClassNotFoundException {
        PPCDatabase ppcDatabase = new PPCDatabase();
        dbManager = new PPCDatabaseManager(ppcDatabase.getConnection());
    }

    @GetMapping("/login")
    public void login(HttpServletResponse resp) throws IOException {
        resp.sendRedirect("/");
    }

    @GetMapping(value = "/")
    public ModelAndView login(HttpSession ses) {
        ModelAndView ret = new ModelAndView("log-in");

        if (ses.getAttribute("error") != null) {
            ret.addObject("error", ses.getAttribute("error"));
            ses.removeAttribute("error");
        }

        if (ses.getAttribute("success") != null) {
            ret.addObject("success", ses.getAttribute("success"));
            ses.removeAttribute("success");
        }

        if (ses.getAttribute("type") != null) {
            ret.addObject("type", ses.getAttribute("type"));
            ses.removeAttribute("type");
        }

        return ret;
    }

    @PostMapping(value = {"/", "/login"})
    public ModelAndView login(HttpServletResponse resp,
                              HttpSession ses,
                              @RequestParam String username,
                              @RequestParam String password) throws IOException, SQLException {
        ModelAndView ret = new ModelAndView("log-in");
        User user = dbManager.getUserByEmail(username);
        if (illegalCredentials(ret, user, password)) return ret;
        ses.setAttribute("user", user);
        resp.sendRedirect("/home");
        return null;
    }

    private boolean illegalCredentials(ModelAndView ret, User user, String password) {
        if (user == null || !user.getPassword().equals(hashPassword(password))) {
            ret.addObject("error", "Incorrect username or password");
            return true;
        }
        return false;
    }

    @RequestMapping("/logout")
    public void logout(HttpServletResponse resp, HttpSession ses) throws IOException {
        ses.invalidate();
        resp.sendRedirect("/");
    }


    public static String hashPassword(String password) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert md != null;

        byte[] messageDigest = md.digest(password.getBytes());

        BigInteger no = new BigInteger(1, messageDigest);

        StringBuilder hashText = new StringBuilder(no.toString(16));

        while (hashText.length() < 32)
            hashText.insert(0, "0");

        return hashText.toString();
    }

}
