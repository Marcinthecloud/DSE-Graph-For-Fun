config create_schema: false, load_new: false, load_threads: 3


// data file paths
list_of_review_data_paths = ['/path/to/reviews.json.gz']
list_of_metadata_paths = ['/path/to/meta.json.gz']
list_of_q_and_a_data_paths = ['/path/to/qa.json.gz']


// data mapper for our review nested maps
reviewE = {
    label 'reviewed'
    outV 'reviewer', {
        label 'Customer'
        key 'customerId'
    }
    inV 'asin', {
        label 'Item'
        key 'asin'
    }
    property 'overall', 'rating'
    property 'reviewTime', 'timestampAsText'
    property 'unixReviewTime', 'timestamp'
}

// transform and load the review data
for (path in list_of_review_data_paths){
    // transforming our review nested maps
    review_data = File.json(path).gzip().transform{
        it['reviewer'] = [:];
        if(it.containsKey('reviewerName')){
          it['reviewer']['name'] = it['reviewerName'];
          it.remove('reviewerName');
        }
        if(it.containsKey('reviewerID')){
          it['reviewer']['customerId'] = it['reviewerID'];
            it.remove('reviewerID');
        }
        if (it.containsKey('unixReviewTime')) {
          it['unixReviewTime'] = java.time.Instant.ofEpochSecond((long)it['unixReviewTime'])
        }
        it['helpful'] = it['helpful'][1]>0?((float)it['helpful'][0] / (float)it['helpful'][1]):0;
        it
    }
    // load data
    load(review_data).asEdges(reviewE)
}

// data mapper for metadata nested maps
itemV = {
    label 'Item'
    key 'asin'
    // Item -(viewed_with)-> Item edge
    outV 'also_viewed', 'viewed_with', {
        label 'Item'
        key 'asin'
    }
    // Item -(bought_after_viewing)-> Item edge
    outV 'buy_after_viewing', 'bought_after_viewing', {
        label 'Item'
        key 'asin'
    }
    // Item -(purchased_with)-> Item edge
    outV 'bought_together', 'purchased_with', {
        label 'Item'
        key 'asin'
    }
    // Item -(also_bought)-> Item edge
    outV 'also_bought', 'also_bought', {
        label 'Item'
        key 'asin'
    }
    // Item -(belongs_in_category)-> Category edge
    outV 'Categories', 'belongs_in_category', {
        label 'Category'
        key 'category', 'name'
    }
    // Item -(has_salesRank)-> Category edge
    outE "SalesRank", "has_salesRank", {
        vertex "category", {
            label "Category"
            key "category", "name"
        }
    }
}

// transform and load the metadata
for (path in list_of_metadata_paths){
    // transforming our metadata nested maps
    metadata = File.json(path).gzip().transform{
        if (it.containsKey("related")){
            for (key in it['related'].keySet())
                it[key] = it['related'][key];
            it.remove('related')
        }
        if (it.containsKey("salesRank")){
            it['SalesRank'] = [];
            for (key in it['salesRank'].keySet())
                it['SalesRank'].add(['category': key, 'rank': it['salesRank'][key]]);
        }
        if (it.containsKey("categories") && it['categories'][0][0]){
            it['Categories'] = [];
            for (subarray in it["categories"]){
                it['Categories'].add(subarray[0]);
            }
        }
        it.remove('categories');
        it.remove('salesRank');
        it
    }
    // load data
    load(metadata).asVertices(itemV)
}

// data mapper for our qa_data nested maps
questionV = {
    label "Question"
    // Item -(has_question)-> Question edge
    inV "asin", "has_question", {
        label "Item"
        key "asin"
    }
    property 'answerTime', 'timestampAsText'
    property 'unixTime', 'timestamp'

}

// transform and load the Q/A data
for (path in list_of_q_and_a_data_paths){
    // no need to transform our qa nested maps
    q_and_a_data = File.json(path).gzip().transform {
      if (it.containsKey('unixTime')) {
        it['unixTime'] = java.time.Instant.ofEpochSecond((long)it['unixTime'])
      }
      it
    }
    // load data
    load(q_and_a_data).asVertices(questionV)
}
