package br.fiap.pos.fastfood.preparation.repository;

import br.fiap.pos.fastfood.preparation.domain.Order;

public interface OrderRepository extends org.springframework.data.mongodb.repository.MongoRepository<Order, String>{
}
