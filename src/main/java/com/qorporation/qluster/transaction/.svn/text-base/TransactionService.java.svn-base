package com.qorporation.qluster.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.qorporation.qluster.common.functional.F2;
import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.entity.Operation;
import com.qorporation.qluster.entity.operation.PrefetchOperation;
import com.qorporation.qluster.entity.operation.interfaces.PrefetchableOperation;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.util.ClassWalker;
import com.qorporation.qluster.util.ClassWalkerFilter;

public class TransactionService extends Service {

	private ConcurrentHashMap<Thread, Transaction> transactions = null;
	private Map<Class<? extends Operation<?, ?>>, List<F2<Transaction, Operation<?, ?>, Void>>> automaticDependencies = null;
	
	@Override
	public void init(ServiceManager serviceManager, Config config) {
		this.logger.info("Loading transaction service");
		
		this.transactions = new ConcurrentHashMap<Thread, Transaction>();
		this.automaticDependencies = new HashMap<Class<? extends Operation<?, ?>>, List<F2<Transaction, Operation<?, ?>, Void>>>();
		
		this.generateAutomaticDependencies();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	private void generateAutomaticDependencies() {		
		Map<Class<?>, F2<Transaction, Operation<?, ?>, Void>> autoInterfaces = new HashMap<Class<?>, F2<Transaction, Operation<?, ?>, Void>>() {{
			this.put(PrefetchableOperation.class, new F2<Transaction, Operation<?, ?>, Void>() {

				@Override
				public Void f(Transaction a, Operation<?, ?> b) {
					b.addPostDependency(new PrefetchOperation(a, (PrefetchableOperation) b));
					return null;
				}
				
			});
		}};
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(Operation.class));
		
		while (itr.hasNext()) {
			Class<? extends Operation<?, ?>> cls = (Class<? extends Operation<?, ?>>) itr.next();
			
			this.logger.info(String.format("Generating automatic dependency list for %s", cls.getName()));
			
			List<F2<Transaction, Operation<?, ?>, Void>> funcs = new ArrayList<F2<Transaction, Operation<?, ?>, Void>>();
			for (Entry<Class<?>, F2<Transaction, Operation<?, ?>, Void>> e: autoInterfaces.entrySet()) {
				if (e.getKey().isAssignableFrom(cls)) {
					this.logger.info(String.format("Found automatic dependency %s for %s", e.getKey().getName(), cls.getName()));
					funcs.add(e.getValue());
				}
			}
			
			this.automaticDependencies.put(cls, funcs);
		}
	}

	private void clearGlobalTransaction() {
		Transaction transaction = this.transactions.remove(Thread.currentThread());
		if (transaction != null) {
			transaction.finish();
		}
	}
	
	public Transaction startGlobalTransaction() {
		clearGlobalTransaction();
		
		Transaction transaction = new Transaction(this);
		this.transactions.put(Thread.currentThread(), transaction);
		
		return transaction;
	}
	
	public Transaction getGlobalTransaction() {
		return this.transactions.get(Thread.currentThread());
	}

	public void addAutomaticDependencies(Transaction transaction, Operation<?, ?> op) {
		for (F2<Transaction, Operation<?, ?>, Void> f: this.automaticDependencies.get(op.getClass())) {
			f.f(transaction, op);
		}
	}
	
}
