package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("/orders")
public class OrdersController {
    private UserDao userDao;
    private ProfileDao profileDao;
    private OrderDao orderDao;
    private ShoppingCartDao shoppingCartDao;

    @Autowired
    public OrdersController(UserDao userDao, ProfileDao profileDao, OrderDao orderDao, ShoppingCartDao shoppingCartDao) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Order> createOrder(Principal principal) {
        try {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);
            Profile profile = profileDao.getByUserId(userId);

            return new ResponseEntity<>(orderDao.add(userId, profile, shoppingCart), HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error");
        }
    }
}
