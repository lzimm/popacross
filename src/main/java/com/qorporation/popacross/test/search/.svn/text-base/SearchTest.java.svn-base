package com.qorporation.popacross.test.search;

import java.util.List;

import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.manager.ItemManager;
import com.qorporation.popacross.logic.ItemLogic;
import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.test.TestRunner;
import com.qorporation.qluster.transaction.Transaction;

public class SearchTest extends TestRunner {
	
	private List<Entity<Item>> testItems = null;
	
	public void setup(EntityService entityService, LogicService logicService) {
		super.setup(entityService, logicService);
		
		Transaction t = this.entityService.startGlobalTransaction();
		this.testItems = this.entityService.getManager(Item.class, ItemManager.class).query(
				new SQLFieldPredicate<Item, String>(
						Item.token, 
						SQLFieldPredicate.Comparator.ISNOTNULL)
						.limit(10));
		t.finish();
	}

	public void testIndex() {
		Transaction t = this.entityService.startGlobalTransaction();
		
		for (Entity<Item> item: this.testItems) {
			this.logger.info(String.format("- adding item: %s", item.get(Item.token)));
			this.logicService.get(ItemLogic.class).addToSearchIndex(item, false);
		}

		this.logicService.get(ItemLogic.class).commitSearchIndex();
		
		t.finish();
	}
	
	public void testSearch() {
		Transaction t = this.entityService.startGlobalTransaction();
		
		for (Entity<Item> item: this.testItems) {
			GeoPoint geo = item.get(Item.position);
			String searchQuery = item.get(Item.label).split(" ")[0];
			List<Entity<Item>> res = this.logicService.get(ItemLogic.class).search(geo, searchQuery);
			for (Entity<Item> r: res) {
				this.logger.info(String.format("- found item: %s", r.get(Item.token)));
			}
		}
		
		t.finish();	
	}
	
}
