package ATKeyLogin.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ATKeyLogin.backend.model.Order;

public interface OrderDAO extends JpaRepository<Order, Long>{
    public Order findByOrderId(String orderId);
}
