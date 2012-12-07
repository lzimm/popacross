package com.qorporation.popacross.search;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter.Side;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.spatial.tier.DistanceQueryBuilder;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.manager.ItemManager;
import com.qorporation.qluster.async.AsyncQueue;
import com.qorporation.qluster.async.AsyncService;
import com.qorporation.qluster.async.AsyncTask;
import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.common.Pair;
import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.RelativePath;
import com.qorporation.qluster.util.UnzipUtil;

@SuppressWarnings("deprecation")
public class SearchService extends Service {

	private class AddItemTask extends AsyncTask<Void> {
		private Entity<Item> item;
		public AddItemTask(Entity<Item> item) { this.item = item; }
		
		@Override
		public Void call() throws Exception {
			Transaction t = SearchService.this.entityService.startGlobalTransaction();
			SearchService.this.addItem(item);
			t.finish();
			return null;
		}
	}
	
	private class RebuildSuggestionTask extends AsyncTask<Void> {
		@Override
		public Void call() throws Exception {
			Transaction t = SearchService.this.entityService.startGlobalTransaction();
			SearchService.this.rebuildSuggestions();
			t.finish();
			return null;
		}
	}
	
	private AsyncService asyncService = null;
	private EntityService entityService = null;
	private ItemManager itemManager = null;
	
	private Analyzer analyzer = null;
	private IndexWriter writer = null;
	
	private static final long SUGGEST_REBUILD_INTERVAL = 60*60*5;
	private AsyncQueue addItemQueue = null;
	private AtomicLong suggestRebuildTime = null;
	private IndexWriter suggestWriter = null;
	
	@Override
	public void init(ServiceManager serviceManager, Config config) {
		this.logger.info("Loading search service");
		
		this.asyncService = serviceManager.getService(AsyncService.class);
		this.entityService = serviceManager.getService(EntityService.class);
		this.itemManager = this.entityService.getManager(Item.class, ItemManager.class);
		
		try {
			Directory dir = FSDirectory.open(new File(RelativePath.root().getAbsolutePath()
									  				.concat(File.separator)
													.concat("data")
													.concat(File.separator)
													.concat("lucene")
													.concat(File.separator)
													.concat("index")));
			
			this.analyzer = new StandardAnalyzer(Version.LUCENE_31);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, this.analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			
			this.writer = new IndexWriter(dir, iwc);
			this.addItemQueue = new AsyncQueue();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		try {
			Directory dir = FSDirectory.open(new File(RelativePath.root().getAbsolutePath()
									  				.concat(File.separator)
													.concat("data")
													.concat(File.separator)
													.concat("lucene")
													.concat(File.separator)
													.concat("suggest")));
			
			Analyzer analyzer = new Analyzer() {
				@Override
	    		public TokenStream tokenStream(String fieldName, Reader reader) {
	    			TokenStream result = new StandardTokenizer(Version.LUCENE_31, reader);

	    			result = new StandardFilter(Version.LUCENE_31,result);
	    			result = new LowerCaseFilter(Version.LUCENE_31,result);
	    			
	    			Set<String> stopWords = new HashSet<String>();
	    			stopWords.addAll(Arrays.asList(
	    				    "a", "an", "and", "are", "as", "at", "be", "but", "by",
	    				    "for", "i", "if", "in", "into", "is",
	    				    "no", "not", "of", "on", "or", "s", "such",
	    				    "t", "that", "the", "their", "then", "there", "these",
	    				    "they", "this", "to", "was", "will", "with"));
	    			result = new StopFilter(Version.LUCENE_31, result, stopWords);
	    			result = new EdgeNGramTokenFilter(result, Side.FRONT, 1, 20);

	    			return result;
	    		}
	    	};
	    	
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			
			this.suggestWriter = new IndexWriter(dir, iwc);
			this.suggestRebuildTime = new AtomicLong(0);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		this.logger.info("Registering shutdown hook for search service");
		this.registerShutdownHook();
	}
	
	public void commitChanges() {
		try {
			this.writer.commit();
			this.suggestWriter.commit();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public void addItemAsync(Entity<Item> item) {
		this.addItemQueue.queue(new AddItemTask(item));
	}
	
	public void addItem(Entity<Item> item) {		
		try {
			QueryParser itemParser = new QueryParser(Version.LUCENE_31, "token", this.analyzer);
			Query query = itemParser.parse(item.get(Item.token));
			this.writer.deleteDocuments(query);

			Document doc = new Document();
			doc.add(new Field("item", item.getKey(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field("token", item.get(Item.token), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("label", item.get(Item.label), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("description", item.get(Item.description), Field.Store.YES, Field.Index.ANALYZED));
		    doc.add(new Field("lat", NumericUtils.doubleToPrefixCoded(item.get(Item.position).getLat()),Field.Store.YES, Field.Index.NOT_ANALYZED));
		    doc.add(new Field("lng", NumericUtils.doubleToPrefixCoded(item.get(Item.position).getLng()),Field.Store.YES, Field.Index.NOT_ANALYZED));
		    
		    IProjector projector = new SinusoidalProjector(); 
		    for (int tier = 5; tier <= 15; tier++) { 
		    	CartesianTierPlotter ctp = new CartesianTierPlotter(tier, projector, "tier_"); 
		    	double boxId = ctp.getTierBoxId(item.get(Item.position).getLat(), item.get(Item.position).getLng()); 
		    	doc.add(new Field(ctp.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)); 
		    } 
		    
			this.writer.addDocument(doc);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		try {
			
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public List<Pair<Float, String>> searchItems(GeoPoint point, String search) {
		if (search == null || search.isEmpty()) {
			return new ArrayList<Pair<Float, String>>();
		}
		
		try {
			search = new StringBuilder(search).append("~").toString();
			
			QueryParser labelParser = new QueryParser(Version.LUCENE_31, "label", this.analyzer);
			QueryParser descriptionParser = new QueryParser(Version.LUCENE_31, "description", this.analyzer);
			
			Query labelQuery = labelParser.parse(search);
			Query descriptionQuery = descriptionParser.parse(search);
			Query query = labelQuery.combine(new Query[]{labelQuery, descriptionQuery});
			
			DistanceQueryBuilder distanceBuilder = new DistanceQueryBuilder(point.getLat(), point.getLng(), 20.0, "lat", "lng", "tier_", true, 5, 15);
			query = distanceBuilder.getQuery(query);
		    
			IndexReader reader = IndexReader.open(this.writer, true);		    
			IndexSearcher searcher = new IndexSearcher(reader);
			
			TopDocs topDocs = searcher.search(query, 100);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			
			List<Pair<Float, String>> ret = new ArrayList<Pair<Float, String>>(scoreDocs.length);
			for (ScoreDoc s: scoreDocs) {
				Document doc = searcher.doc(s.doc);
				String key = doc.get("item");
				ret.add(new Pair<Float, String>(s.score, key));
			}
			
			searcher.close();
		    reader.close();
			
			return ret;
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return null;
	}
	
	public List<Entity<Item>> searchItemEntities(GeoPoint point, String search) {
		List<Pair<Float, String>> keyScores = this.searchItems(point, search);
		if (keyScores == null) {
			return null;
		}
		
		List<String> keys = UnzipUtil.bs(keyScores);
		Map<String, Entity<Item>> items = this.itemManager.get(keys);

		return new ArrayList<Entity<Item>>(items.values());
	}
	
	public List<String> suggest(String search) {
		this.checkSuggestionRebuildStatus();
		
		try {
		   	Query query = new TermQuery(new Term("words", search));
	    	Sort sort = new Sort(new SortField("count", SortField.INT));
	
	    	IndexReader suggestReader = IndexReader.open(this.suggestWriter, true);
	    	IndexSearcher suggestSearcher = new IndexSearcher(suggestReader);
	    	
	    	TopDocs docs = suggestSearcher.search(query, null, 20, sort);
	    	ScoreDoc[] scoreDocs = docs.scoreDocs;
	    	
	    	List<String> ret = new ArrayList<String>(scoreDocs.length);
	    	for (ScoreDoc doc: scoreDocs) {
	    		ret.add(suggestReader.document(doc.doc).get("source"));
	    	}
	    	
	    	suggestSearcher.close();
	    	suggestReader.close();
	
	    	return ret;
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return null;
	}

	private void checkSuggestionRebuildStatus() {
		long lastRebuild = this.suggestRebuildTime.get();
		if ((System.currentTimeMillis() - lastRebuild) < SearchService.SUGGEST_REBUILD_INTERVAL) {
			return;
		}
		
		if (!this.suggestRebuildTime.compareAndSet(lastRebuild, System.currentTimeMillis())) {
			return;
		}
		
		this.asyncService.queue(new RebuildSuggestionTask());
	}
	
	private void rebuildSuggestions() {
		try {
			IndexReader sourceReader = IndexReader.open(this.writer, true);
			LuceneDictionary dict = new LuceneDictionary(sourceReader, "label");
			
			Iterator<String> words = dict.getWordsIterator();
			while (words.hasNext()) {
				String word = words.next();
				int wordFreq = sourceReader.docFreq(new Term("label", word));
				
				QueryParser itemParser = new QueryParser(Version.LUCENE_31, "source", new StandardAnalyzer(Version.LUCENE_31));
				Query query = itemParser.parse(word);
				this.suggestWriter.deleteDocuments(query);
				
	    		Document doc = new Document();
	    		doc.add(new Field("source", word, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    		doc.add(new Field("words", word, Field.Store.YES, Field.Index.ANALYZED));
	    		doc.add(new Field("count", Integer.toString(wordFreq), Field.Store.NO, Field.Index.NOT_ANALYZED));
	
	    		this.suggestWriter.addDocument(doc);
			}
			
			sourceReader.close();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

	public void endpointAddItem(String key) {
		Transaction t = this.entityService.startGlobalTransaction();
		
		try {
			Entity<Item> item = this.itemManager.get(key);
			this.addItem(item);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		t.finish();
	}

	public List<Pair<Float, String>> endpointSearchItems(GeoPoint geo, String search) {
		Transaction t = this.entityService.startGlobalTransaction();
		
		List<Pair<Float, String>> ret = this.searchItems(geo, search);
		
		t.finish();
		return ret;
	}
	
	private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
            	try {
            		SearchService.this.logger.info("Shutting down search service");
            		SearchService.this.writer.close();
            		SearchService.this.suggestWriter.close();
            	} catch (Exception e) {
            		ErrorControl.logException(e);
            	}
            }
        });	
	}

}
