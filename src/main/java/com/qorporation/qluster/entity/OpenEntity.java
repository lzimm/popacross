package com.qorporation.qluster.entity;

import com.qorporation.qluster.conn.Connection;

public class OpenEntity<T extends Definition<? extends Connection>> extends Entity<T> {

	public OpenEntity(EntityService service, Class<T> type, String key,
			Node<T> node) {
		super(service, type, key, node);
	}

}
