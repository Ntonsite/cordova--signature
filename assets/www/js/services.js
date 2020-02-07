angular.module('your_app_name.services', [])

//SHARE DARA SERVICE
.service('sharedProperties', function () {
    var property = {};

    return {
        getProperty: function () {
            return property;
        },
        setProperty: function(value) {
            property = value;
        }
    };
})

// WP POSTS RELATED FUNCTIONS
.service('PostService', function ($rootScope, $http, $q, $ionicLoading, $state, WORDPRESS_API_URL, AuthService, BookMarkService, SaveAccountService){

  this.doAccountOpening = function(account_data) {
    
    account_data.post_author=JSON.parse(window.localStorage.ionWordpress_user).user_id;
    var deferred = $q.defer(),
        nonce_dfd = $q.defer(),
        authService = this;

    AuthService.requestNonce("posts", "create_account")
    .then(function(nonce){
      nonce_dfd.resolve(nonce);
    });
    

    if(nonce_dfd.promise.$$state.status===0){
      SaveAccountService.saveAccount(account_data, true);
      $ionicLoading.hide();
      $state.go('app.home');
    }

    nonce_dfd.promise.then(function(nonce){
        var deferred = $q.defer();
        var parameters='';
        for (var key in account_data) {
          if (account_data.hasOwnProperty(key)) {
            parameters=parameters+'&'+key+'='+account_data[key];
          }
        }

        

        $http({
            method:'POST',
            url:WORDPRESS_API_URL+'posts/create_account/?callback=JSON_CALLBACK'+'&nonce='+ nonce +'&insecure=cool',
            data: parameters,
            headers : {'Content-Type': 'application/x-www-form-urlencoded'}
        }).then(function(response){
            //success
           
            if(response.statusText=='OK'){
              var title=account_data.Last_Name.toUpperCase()+"/"+account_data.Middle_Name.toUpperCase()+","+account_data.First_Name.toUpperCase();
              SaveAccountService.removeSavedAccount(title);
              $state.go('app.home');
               $ionicLoading.hide();
            }else{
              SaveAccountService.saveAccount(account_data, true);
               $ionicLoading.hide();
              $state.go('app.home');
            }
            
        }, function(response){
            //error
            SaveAccountService.saveAccount(account_data, true);
            $ionicLoading.hide();
            $state.go('app.home');
        });
    });

    return deferred.promise;
  };

  this.getRecentPosts = function(page) {
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'get_recent_posts/' +
      '?page='+ page +
      '&post_type=account'+
      '&post_author='+JSON.parse(window.localStorage.getItem('ionWordpress_user')).user_id+
      '&callback=JSON_CALLBACK'+
      '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  this.getApprovedPosts = function(page) {
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'get_approved_posts/' +
      '?page='+ page +
      '&post_type=account'+
      '&post_status=publish'+
      '&post_author='+JSON.parse(window.localStorage.getItem('ionWordpress_user')).user_id+
      '&callback=JSON_CALLBACK'+
      '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  this.getRejectedPosts = function(page) {
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'get_rejected_posts/' +
      '?page='+ page +
      '&post_type=account'+
      '&post_status=trash'+
      '&post_author='+JSON.parse(window.localStorage.getItem('ionWordpress_user')).user_id+
      '&callback=JSON_CALLBACK'+
      '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  this.getUserGravatar = function(userId){
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'user/get_avatar/' +
    '?user_id='+ userId +
    '&type=full' +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {

      var avatar_aux = data.avatar.replace("http:", "");
      var avatar = 'http:' + avatar_aux;

      deferred.resolve(avatar);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  this.getPost = function(postId) {
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'get_post/' +
      '?post_id='+ postId +
      '&post_type=account'+
      '&post_author='+JSON.parse(window.localStorage.getItem('ionWordpress_user')).user_id+
      '&callback=JSON_CALLBACK'+
      '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  this.submitComment = function(postId, content) {
    var deferred = $q.defer(),
        user = AuthService.getUser();

    $http.jsonp(WORDPRESS_API_URL + 'user/post_comment/' +
    '?post_id='+ postId +
    '&cookie='+ user.cookie +
    '&comment_status=1' +
    '&content='+ content +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  this.getPostsFromCategory = function(categoryId, page) {
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'get_category_posts/' +
    '?id='+ categoryId +
    '&page='+ page +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  this.shortenPosts = function(posts) {
    //we will shorten the post
    //define the max length (characters) of your post content
    var maxLength = 600;
    return _.map(posts, function(post){
      if(post.content.length > maxLength){
        //trim the string to the maximum length
        var trimmedString = post.content.substr(0, maxLength);
        //re-trim if we are in the middle of a word
        trimmedString = trimmedString.substr(0, Math.min(trimmedString.length, trimmedString.lastIndexOf("</p>")));
        post.content = trimmedString;
      }
      return post;
    });
  };

  this.sharePost = function(link){
    window.plugins.socialsharing.share('Check this post here: ', null, null, link);
  };

  this.bookmarkPost = function(post){
    BookMarkService.bookmarkPost(post);
    $rootScope.$broadcast("new-bookmark", post);
  };

  this.saveAccount = function(account, valid){
    SaveAccountService.saveAccount(account, valid);
    $rootScope.$broadcast("new-bookmark", account);
  };

  this.getWordpressPage = function(page_slug) {
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'get_page/' +
    '?slug='+ page_slug +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });
    return deferred.promise;
  };

})


// SEARCH MENU RELATED FUNCTIONS
.service('SearchService', function ($rootScope, $http, $q, WORDPRESS_API_URL){

  this.search = function(query) {

    var search_results = [],
        search_results_response = $q.defer(),
         promises = [
          this.searchPosts(query),
          this.searchTags(query),
          this.searchAuthors(query)
        ];

    $q.all(promises).then(function(promises_values){
      _.map(promises_values, function(promise_value){
        search_results.push({
          _id: promise_value.id,
          results:_.map(promise_value.posts, function(post){
            return {
              title: post.title,
              id:post.id,
              date: post.date,
              excerpt: post.excerpt
            };
          })
        });
      });
      search_results_response.resolve(search_results);
    });

    return search_results_response.promise;
  };

  this.searchPosts = function(query) {
    var deferred = $q.defer();

    $http.jsonp(WORDPRESS_API_URL + 'get_search_results/' +
    '?search='+ query +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      var promise_value = {
        id : "posts",
        posts : data.posts
      };
      deferred.resolve(promise_value);
    })
    .error(function(data) {
      deferred.reject(data);
    });
    return deferred.promise;
  };

  this.searchTags = function(query) {
    var tags_deferred = $q.defer(),
        results_deferred = $q.defer();

    //get all tags and filter the ones with the query in the title
    $http.jsonp(WORDPRESS_API_URL + 'get_tag_index/' +
    '?callback=JSON_CALLBACK')
    .success(function(data) {
      var tags = _.filter(data.tags, function(tag){
        return ((tag.title.indexOf(query) > -1));
                // || (tag.description.indexOf(query) > -1));
      });
      tags_deferred.resolve(tags);
    })
    .error(function(data) {
      tags_deferred.reject(data);
    });

    tags_deferred.promise.then(function(tags){
      //for each of the tags matching the query, bring the related posts
      var tag_promises = _.map(tags, function(tag){
        return $http.jsonp(WORDPRESS_API_URL + 'get_tag_posts/' +
          '?id='+ tag.id +
          '&callback=JSON_CALLBACK'+
          '&insecure=cool');
      });

      //prepare the response
      $q.all(tag_promises).then(function(results){
        var posts = [];
        _.map(results, function(result){
          _.each(result.data.posts, function(post){
            posts.push(post);
          });
        });
        var promise_value = {
          id : "tags",
          posts : posts
        };
        results_deferred.resolve(promise_value);
      });
    });

    return results_deferred.promise;
  };

  this.searchAuthors = function(query) {
    var authors_deferred = $q.defer(),
        results_deferred = $q.defer();

    //get all the authors and filter the ones with the query
    $http.jsonp(WORDPRESS_API_URL + 'get_author_index/' +
    '?callback=JSON_CALLBACK')
    .success(function(data) {
      var authors = _.filter(data.authors, function(author){
        return ((author.name.indexOf(query) > -1) || (author.nickname.indexOf(query) > -1) || (author.first_name.indexOf(query) > -1));
      });
      authors_deferred.resolve(authors);
    })
    .error(function(data) {
      authors_deferred.reject(data);
    });

    authors_deferred.promise.then(function(authors){
      //for each of the tags matching the query, bring the related posts
      var author_promises = _.map(authors, function(author){
        return $http.jsonp(WORDPRESS_API_URL + 'get_author_posts/' +
        '?id='+ author.id +
        '&callback=JSON_CALLBACK'+
        '&insecure=cool');
      });

      //prepare the response
      $q.all(author_promises).then(function(results){
        var posts = [];
        _.map(results, function(result){
          _.each(result.data.posts, function(post){
            posts.push(post);
          });
        });

        var promise_value = {
          id : "authors",
          posts : posts
        };
        results_deferred.resolve(promise_value);
      });
    });

    return results_deferred.promise;
  };
})


// BOOKMARKS FUNCTIONS
.service('BookMarkService', function (_){
  this.bookmarkPost = function(bookmark_post){
    var user_bookmarks = !_.isUndefined(window.localStorage.ionWordpress_bookmarks) ? JSON.parse(window.localStorage.ionWordpress_bookmarks) : [];

    //check if this post is already saved
    var existing_post = _.find(user_bookmarks, function(post){ return post.id == bookmark_post.id; });

    if(!existing_post){
      user_bookmarks.push({
        id: bookmark_post.id,
        title : bookmark_post.title,
        date: bookmark_post.date,
        excerpt: bookmark_post.excerpt
      });
    }

    window.localStorage.ionWordpress_bookmarks = JSON.stringify(user_bookmarks);
  };

  this.getBookmarks = function(){
    return JSON.parse(window.localStorage.ionWordpress_bookmarks || '[]');
  };

  this.remove = function(id){
    var user_bookmarks = !_.isUndefined(window.localStorage.ionWordpress_bookmarks) ? JSON.parse(window.localStorage.ionWordpress_bookmarks) : [];

    //check if this post is already saved
    var remaining_posts = _.filter(user_bookmarks, function(bookmark){ return bookmark.id != id; });

    window.localStorage.ionWordpress_bookmarks = JSON.stringify(remaining_posts);
  };
})

// SAVE ACCOUNT FUNCTIONS
.service('SaveAccountService', function (_){
  this.saveAccount = function(account, valid){
    var saved_accounts = !_.isUndefined(window.localStorage.getItem('ionWordpress_bookmarks')) ? JSON.parse(window.localStorage.getItem('ionWordpress_bookmarks')) : [];

    //check if this post is already saved
    var title=account.Last_Name.toUpperCase()+"/"+account.Middle_Name.toUpperCase()+","+account.First_Name.toUpperCase();
    this.removeSavedAccount(title);
    
    var existing_accounts = _.find(saved_accounts, function(post){ return post.title == title; });

    if(!existing_accounts){
      saved_accounts.push({
        title : title,
        date:  Date.now(),
        data: JSON.stringify(account),
        valid: valid
      });
    }

    window.localStorage.setItem('ionWordpress_bookmarks', JSON.stringify(saved_accounts));
  };

  this.getSavedAccounts = function(){
    return JSON.parse(window.localStorage.getItem('ionWordpress_bookmarks') || '[]');
  };

  this.removeSavedAccount = function(title){
    var saved_accounts = !_.isUndefined(window.localStorage.getItem('ionWordpress_bookmarks')) ? JSON.parse(window.localStorage.getItem('ionWordpress_bookmarks')) : [];

    //check if this post is already saved
    var remaining_accounts = _.filter(saved_accounts, function(bookmark){ return bookmark.title != title; });

    window.localStorage.setItem('ionWordpress_bookmarks', JSON.stringify(remaining_accounts));
  };
})


// WP AUTHENTICATION RELATED FUNCTIONS
.service('AuthService', function ($rootScope, $http, $q, WORDPRESS_API_URL){

  this.validateAuth = function(user) {
    var deferred = $q.defer();
    $http.jsonp(WORDPRESS_API_URL + 'user/validate_auth_cookie/' +
    '?cookie='+ user.cookie +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });
    return deferred.promise;
  };

  this.doLogin = function(user) {
    var deferred = $q.defer(),
        nonce_dfd = $q.defer(),
        authService = this;

    authService.requestNonce("user", "generate_auth_cookie")
    .then(function(nonce){
      nonce_dfd.resolve(nonce);
    });

    nonce_dfd.promise.then(function(nonce){
      //now that we have the nonce, ask for the new cookie
      authService.generateAuthCookie(user.userName, user.password, nonce)
      .then(function(data){
        if(data.status == "error"){
          // return error message
          deferred.reject(data.error);
        }else{
          //recieve and store the user's cookie in the local storage
          var user = {
            cookie: data.cookie,
            data: data.user,
            user_id: data.user.id
          };

          authService.saveUser(user);

          //getavatar in full size
          authService.updateUserAvatar().then(function(){
            deferred.resolve(user);
          });
        }
      });
    });
    return deferred.promise;
  };

  this.doRegister = function(user) {
    var deferred = $q.defer(),
        nonce_dfd = $q.defer(),
        authService = this;

    authService.requestNonce("user", "register")
    .then(function(nonce){
      nonce_dfd.resolve(nonce);
    });

    nonce_dfd.promise.then(function(nonce){
      authService.registerUser(user.userName, user.email,
        user.displayName, user.password, nonce)
      .then(function(data){
        if(data.status == "error"){
          // return error message
          deferred.reject(data.error);
        }else{
          // in order to get all user data we need to call this function
          // because the register does not provide user data
          authService.doLogin(user).then(function(){
            deferred.resolve(user);
          });
        }
      });
    });

    return deferred.promise;
  };

  this.requestNonce = function(controller, method) {
    var deferred = $q.defer();
    $http.jsonp(WORDPRESS_API_URL + 'get_nonce/' +
    '?controller=' + controller +
    '&method=' + method +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data.nonce);
    })
    .error(function(data) {
      if(data== 'undefined'){
        
      }else{
        deferred.reject(data.nonce);
      }
      
    });
    return deferred.promise;
  };

  this.doForgotPassword = function(username) {
    var deferred = $q.defer();
    $http.jsonp(WORDPRESS_API_URL + 'user/retrieve_password/' +
    '?user_login='+ username +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });
    return deferred.promise;
  };

  this.generateAuthCookie = function(username, password, nonce) {
    var deferred = $q.defer();
    $http.jsonp(WORDPRESS_API_URL + 'user/generate_auth_cookie/' +
    '?username='+ username +
    '&password=' + password +
    '&nonce='+ nonce +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });
    return deferred.promise;
  };

  this.saveUser = function(user){
    window.localStorage.ionWordpress_user = JSON.stringify(user);
  };

  this.getUser = function(){

    var data = (window.localStorage.ionWordpress_user) ? JSON.parse(window.localStorage.ionWordpress_user).data : null,
        cookie = (window.localStorage.ionWordpress_user) ? JSON.parse(window.localStorage.ionWordpress_user).cookie : null;

    return {
      avatar : JSON.parse(window.localStorage.ionWordpress_user_avatar || null),
      data: data,
      cookie: cookie
    };
  };

  this.registerUser = function(username, email, displayName, password, nonce) {
    var deferred = $q.defer();
    $http.jsonp(WORDPRESS_API_URL + 'user/register/' +
    '?username='+ username +
    '&email=' + email +
    '&display_name='+ displayName +
    '&user_pass=' + password +
    '&nonce='+ nonce +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });
    return deferred.promise;
  };

  this.userIsLoggedIn = function(){
    var deferred = $q.defer();

    var user = JSON.parse(window.localStorage.ionWordpress_user || null);
    if(user !== null && user.cookie !== null)
    {
      this.validateAuth(user).then(function(data){
        deferred.resolve(data.valid);
      });
    }
    else
    {
      deferred.resolve(false);
    }
    return deferred.promise;
  };

  this.logOut = function(){
    //empty user data

    window.localStorage.ionWordpress_user = null;
    window.localStorage.ionWordpress_user_avatar = null;
    // window.localStorage.ionWordpress_bookmarks = null;
  };

  //update user avatar from WP
  this.updateUserAvatar = function() {
    var avatar_dfd = $q.defer(),
        authService = this,
        user = JSON.parse(window.localStorage.ionWordpress_user || null);

    $http.jsonp(WORDPRESS_API_URL + 'user/get_avatar/' +
    '?user_id='+ user.user_id +
    '&type=full' +
    '&callback=JSON_CALLBACK'+
    '&insecure=cool')
    .success(function(data) {

      var avatar = data.avatar;

      window.localStorage.ionWordpress_user_avatar =  JSON.stringify(avatar);

      avatar_dfd.resolve(avatar);
    })
    .error(function(err) {
      avatar_dfd.reject(err);
    });

    return avatar_dfd.promise;
  };
})

;
