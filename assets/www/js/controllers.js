angular.module('your_app_name.controllers', [])

// APP - RIGHT MENU
.controller('AppCtrl', function($scope, $rootScope, AuthService, SaveAccountService, sharedProperties) {
  
  $scope.$on('$ionicView.enter', function(){
    // Refresh user data & avatar
    $scope.user = AuthService.getUser();

    $scope.hideBackButton = false;
    $scope.formData={};
    $scope.concat_collection=function(obj1, obj2) {
                                var i;
                                var arr  = [];
                                var len1 = obj1.length;
                                var len2 = obj2.length;
                                for (i=0; i<len1; i++) {
                                    arr.push(obj1[i]);
                                }
                                for (i=0; i<len2; i++) {
                                    arr.push(obj2[i]);
                                }
                                return arr;
                            };

    $scope.capture_inputs_values=function(){
          var inputs=$scope.concat_collection(document.getElementsByTagName("input"), document.getElementsByTagName("select"));
          $scope.formData=sharedProperties.getProperty();

          for(var i=0; i<inputs.length; i++){
            if(inputs[i].name=='user_name' || inputs[i].password=='password'){
              continue;
            }
             delete $scope.formData[inputs[i].name];
             if(inputs[i].name!==undefined){
              $scope.formData[inputs[i].name]=inputs[i].value;
             }else{
              $scope.formData[inputs[i].name] =inputs[i].value || '';
             }
          }

          SaveAccountService.saveAccount($scope.formData, false);
          window.localStorage.setItem('formData', JSON.stringify($scope.formData) );
          sharedProperties.setProperty($scope.formData);
          console.log($scope.formData);
    };

    $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
      if ((toState.name == 'done') || (toState.name == 'step1'))
        $scope.hideBackButton = true;
      else 
        $scope.hideBackButton = false;
    });
  });
})

// BOOKMARKS
.controller('BookMarksCtrl', function($scope, $rootScope, $state, $ionicLoading, $timeout, SaveAccountService, PostService, sharedProperties) {

  $scope.bookmarks = SaveAccountService.getSavedAccounts(); 

  // When a new post is bookmarked, we should update bookmarks list
  $rootScope.$on("new-bookmark", function(event, post_id){
    $scope.bookmarks = SaveAccountService.getSavedAccounts();
  });

  $scope.remove = function(title) {
    SaveAccountService.removeSavedAccount(title);
    $scope.bookmarks = SaveAccountService.getSavedAccounts();
  };

  $scope.upload = function(title, accountData){

    PostService.doAccountOpening(JSON.parse(accountData))
      .then(function(account){
        $scope.remove(title);
      },function(err){
        console.log(err);
        $scope.error = err;
      });
  };

  $scope.edit=function(data){

    $ionicLoading.show({
        template: 'Please Wait'
    });

    $timeout(function(){
     sharedProperties.setProperty(JSON.parse(data)); 
     $ionicLoading.hide();
    $state.go('app.step1');
    }, 3000);
    
  };

})


// SETTINGS
.controller('SettingCtrl', function($scope, $ionicActionSheet, $ionicModal, $state, AuthService) {
  $scope.notifications = true;
  $scope.sendLocation = false;

  $ionicModal.fromTemplateUrl('views/common/terms.html', {
    scope: $scope,
    animation: 'slide-in-up'
  }).then(function(modal) {
    $scope.terms_modal = modal;
  });

  $ionicModal.fromTemplateUrl('views/common/faqs.html', {
    scope: $scope,
    animation: 'slide-in-up'
  }).then(function(modal) {
    $scope.faqs_modal = modal;
  });

  $ionicModal.fromTemplateUrl('views/common/credits.html', {
    scope: $scope,
    animation: 'slide-in-up'
  }).then(function(modal) {
    $scope.credits_modal = modal;
  });

  $scope.showTerms = function() {
    $scope.terms_modal.show();
  };

  $scope.showFAQS = function() {
    $scope.faqs_modal.show();
  };

  $scope.showCredits = function() {
    $scope.credits_modal.show();
  };

  // Triggered on a the logOut button click
  $scope.showLogOutMenu = function() {

    var hideSheet = $ionicActionSheet.show({
      destructiveText: 'Logout',
      titleText: 'Are you sure you want to logout? ',
      cancelText: 'Cancel',
      cancel: function() {
        // add cancel code..
      },
      buttonClicked: function(index) {
        //Called when one of the non-destructive buttons is clicked,
        //with the index of the button that was clicked and the button object.
        //Return true to close the action sheet, or false to keep it opened.
        return true;
      },
      destructiveButtonClicked: function(){
        //Called when the destructive button is clicked.
        //Return true to close the action sheet, or false to keep it opened.
        AuthService.logOut();
        $state.go('login', {}, {reload: true});
      }
    });
  };
})

// WALKTHROUGH
.controller('WalkthroughCtrl', function($scope, $state, $ionicSlideBoxDelegate) {

  $scope.$on('$ionicView.enter', function(){
    //this is to fix ng-repeat slider width:0px;
    $ionicSlideBoxDelegate.$getByHandle('walkthrough-slider').update();
  });
})

//LOGIN
.controller('LoginCtrl', function($scope, $state, $ionicLoading, AuthService) {
  $scope.user = {};

  $scope.doLogin = function(){

    $ionicLoading.show({
      template: 'Loging in...'
    });

    var user = {
      userName: $scope.user.userName,
      password: $scope.user.password
    };

    AuthService.doLogin(user)
    .then(function(user){
      //success
      console.log(user);
      $state.go('app.home');

      $ionicLoading.hide();
    },function(err){
      $ionicLoading.hide();
      $ionicLoading.show({
        template: 'Username or password is incorrect'
      });
      $timeout(function(){
       $ionicLoading.hide();
       $state.go('login',{}, {reload: true});
      }, 1000);
      $scope.error = err;
    });

  };
})


// FORGOT PASSWORD
.controller('ForgotPasswordCtrl', function($scope, $state, $ionicLoading, AuthService) {
  $scope.user = {};

  $scope.recoverPassword = function(){

    $ionicLoading.show({
      template: 'Recovering password...'
    });

    AuthService.doForgotPassword($scope.user.userName)
    .then(function(data){
      if(data.status == "error"){
        $scope.error = data.error;
      }else{
        $scope.message ="Link for password reset has been emailed to you. Please check your email.";
      }
      $ionicLoading.hide();
    });
  };
})


// REGISTER
.controller('RegisterCtrl', function($scope, $state, $ionicLoading, AuthService) {
  $scope.user = {};

  $scope.doRegister = function(){

    $ionicLoading.show({
      template: 'Registering user...'
    });

    var user = {
      userName: $scope.user.userName,
      password: $scope.user.password,
      email: $scope.user.email,
      displayName: $scope.user.displayName
    };

    AuthService.doRegister(user)
    .then(function(user){
      //success
      $state.go('app.home');
      $ionicLoading.hide();
    },function(err){
      //err
      $scope.error = err;
      $ionicLoading.hide();
    });
  };
})

// HOME - GET RECENT POSTS
.controller('HomeCtrl', function($scope, $rootScope, $state, $ionicLoading, $timeout, PostService) {
  $scope.posts = [];
  $scope.page = 1;
  $scope.totalPages = 1;

  $scope.doRefresh = function() {
    $ionicLoading.show({
      template: 'Loading accounts...'
    });

    //Always bring me the latest posts => page=1
    PostService.getRecentPosts(1)
    .then(function(data){

      $scope.totalPages = data.pages;
      $scope.posts = PostService.shortenPosts(data.posts);
      $ionicLoading.hide();
      $scope.$broadcast('scroll.refreshComplete');
    },
    function(err){
      $ionicLoading.hide();
      $ionicLoading.show({
        template: 'An error occured'
      });
      $timeout(function(){
       $ionicLoading.hide();
      }, 1000);
      $scope.error = err;
    });
  };

  $scope.loadMoreData = function(){
    $scope.page += 1;

    PostService.getRecentPosts($scope.page)
    .then(function(data){
      //We will update this value in every request because new posts can be created
      $scope.totalPages = data.pages;
      var new_posts = PostService.shortenPosts(data.posts);
      $scope.posts = $scope.posts.concat(new_posts);

      $scope.$broadcast('scroll.infiniteScrollComplete');
    });
  };

  $scope.moreDataCanBeLoaded = function(){
    return $scope.totalPages > $scope.page;
  };

  $scope.sharePost = function(link){
    PostService.sharePost(link);
  };

  $scope.bookmarkPost = function(post){
    $ionicLoading.show({ template: 'Post Saved!', noBackdrop: true, duration: 1000 });
    PostService.bookmarkPost(post);
  };

  $scope.doRefresh();

})

//APPROVED ACCOUNTS CONROLLER
.controller('ApprovedCtrl', function($scope, $rootScope, $state, $ionicLoading, $timeout, PostService) {
  $scope.posts = [];
  $scope.page = 1;
  $scope.totalPages = 1;

  $scope.doRefresh = function() {
    $ionicLoading.show({
      template: 'Loading accounts...'
    });

    //Always bring me the latest posts => page=1
    PostService.getApprovedPosts(1)
    .then(function(data){

      $scope.totalPages = data.pages;
      $scope.posts = PostService.shortenPosts(data.posts);
      $ionicLoading.hide();
      $scope.$broadcast('scroll.refreshComplete');
    },
    function(err){
      $ionicLoading.hide();
      $ionicLoading.show({
        template: 'An error occured'
      });
      $timeout(function(){
       $ionicLoading.hide();
      }, 1000);
      $scope.error = err;
    });
  };

  $scope.loadMoreData = function(){
    $scope.page += 1;

    PostService.getRecentPosts($scope.page)
    .then(function(data){
      //We will update this value in every request because new posts can be created
      $scope.totalPages = data.pages;
      var new_posts = PostService.shortenPosts(data.posts);
      $scope.posts = $scope.posts.concat(new_posts);

      $scope.$broadcast('scroll.infiniteScrollComplete');
    });
  };

  $scope.moreDataCanBeLoaded = function(){
    return $scope.totalPages > $scope.page;
  };

  $scope.sharePost = function(link){
    PostService.sharePost(link);
  };

  $scope.bookmarkPost = function(post){
    $ionicLoading.show({ template: 'Post Saved!', noBackdrop: true, duration: 1000 });
    PostService.bookmarkPost(post);
  };

  $scope.doRefresh();

})

//REJECTED ACCOUNTS CONROLLER
.controller('RejectedCtrl', function($scope, $rootScope, $state, $ionicLoading, $timeout, PostService) {
  $scope.posts = [];
  $scope.page = 1;
  $scope.totalPages = 1;

  $scope.doRefresh = function() {
    $ionicLoading.show({
      template: 'Loading accounts...'
    });

    //Always bring me the latest posts => page=1
    PostService.getRejectedPosts(1)
    .then(function(data){

      $scope.totalPages = data.pages;
      $scope.posts = PostService.shortenPosts(data.posts);
      $ionicLoading.hide();
      $scope.$broadcast('scroll.refreshComplete');
    },
    function(err){
      $ionicLoading.hide();
      $ionicLoading.show({
        template: 'An error occured'
      });
      $timeout(function(){
       $ionicLoading.hide();
      }, 1000);
      $scope.error = err;
    });
  };

  $scope.loadMoreData = function(){
    $scope.page += 1;

    PostService.getRecentPosts($scope.page)
    .then(function(data){
      //We will update this value in every request because new posts can be created
      $scope.totalPages = data.pages;
      var new_posts = PostService.shortenPosts(data.posts);
      $scope.posts = $scope.posts.concat(new_posts);

      $scope.$broadcast('scroll.infiniteScrollComplete');
    });
  };

  $scope.moreDataCanBeLoaded = function(){
    return $scope.totalPages > $scope.page;
  };

  $scope.sharePost = function(link){
    PostService.sharePost(link);
  };

  $scope.bookmarkPost = function(post){
    $ionicLoading.show({ template: 'Post Saved!', noBackdrop: true, duration: 1000 });
    PostService.bookmarkPost(post);
  };

  $scope.doRefresh();

})

//controllers for form-steps
.controller('Step1Ctrl', function($scope, sharedProperties) {
  $scope.formData=sharedProperties.getProperty();
  console.log($scope.formData); 
  $scope.step1Submitted = false; 
  


  if($scope.formData!==undefined){
    $scope.First_Name=$scope.formData.First_Name!==undefined ? $scope.formData.First_Name : '';  
    $scope.Middle_Name=$scope.formData.Middle_Name!==undefined ? $scope.formData.Middle_Name : '';  
    $scope.Last_Name=$scope.formData.Last_Name!==undefined ? $scope.formData.Last_Name : ''; 
    $scope.Date_Of_Birth=$scope.formData.Date_Of_Birth!==undefined ? $scope.formData.Date_Of_Birth : ''; 
    $scope.Birth_Region=$scope.formData.Birth_Region!==undefined ? $scope.formData.Birth_Region : ''; 
    $scope.Birth_District=$scope.formData.Birth_District!==undefined ? $scope.formData.Birth_District : ''; 
    $scope.Birth_Ward=$scope.formData.Birth_Ward!==undefined ? $scope.formData.Birth_Ward : ''; 
    $scope.Marriage=$scope.formData.Marriage!==undefined ? $scope.formData.Marriage : ''; 
    $scope.Gender=$scope.formData.Gender!==undefined ? $scope.formData.Gender : ''; 
    $scope.Father_Name=$scope.formData.Father_Name!==undefined ? $scope.formData.Father_Name : ''; 
    $scope.Mother_Name=$scope.formData.Mother_Name!==undefined ? $scope.formData.Mother_Name : ''; 
    $scope.Account_Type=$scope.formData.Account_Type!==undefined ? $scope.formData.Account_Type : ''; 
    $scope.Number_of_Family_Members=$scope.formData.Number_of_Family_Members!==undefined ? $scope.formData.Number_of_Family_Members : ''; 
  }
  
  $scope.submit = function() {
    $scope.step1Submitted = true;
    $scope.capture_inputs_values();
  };
})

.controller('Step1FormCtrl', function($scope, $rootScope, $state) {
  var validate = $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
    if (($scope.step1Form.$invalid) && (toState.data.step > fromState.data.step))
      event.preventDefault();
  });

  $scope.$on('$destroy', validate);
})

.controller('Step2Ctrl', function($scope, sharedProperties) {
  $scope.step2Submitted = false;
  $scope.formData=sharedProperties.getProperty();
  console.log($scope.formData); 

  if($scope.formData!==undefined){ 
    $scope.Nationality=$scope.formData.Nationality!==undefined ? $scope.formData.Nationality : '';  
    $scope.Education_Level=$scope.formData.Education_Level!==undefined ? $scope.formData.Education_Level : '';  
    $scope.Identity_Type=$scope.formData.Identity_Type!==undefined ? $scope.formData.Identity_Type : '';
    $scope.Identity_Number=$scope.formData.Identity_Number!==undefined ? $scope.formData.Identity_Number : '';
    $scope.Issue_Date=$scope.formData.Issue_Date!==undefined ? $scope.formData.Issue_Date : '';
    $scope.Issue_Place=$scope.formData.Issue_Place!==undefined ? $scope.formData.Issue_Place : '';
    $scope.Residence_Region=$scope.formData.Residence_Region!==undefined ? $scope.formData.Residence_Region : '';
    $scope.Residence_District=$scope.formData.Residence_District!==undefined ? $scope.formData.Residence_District : '';
    $scope.Residence_Ward=$scope.formData.Residence_Ward!==undefined ? $scope.formData.Residence_Ward : '';
    $scope.Residence_Since=$scope.formData.Residence_Since!==undefined ? $scope.formData.Residence_Since : '';
  }

  $scope.submit = function() {
    $scope.step2Submitted = true;
    $scope.capture_inputs_values();
  };
})

.controller('Step2FormCtrl', function($scope, $rootScope, $state) {
  var validate = $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
    if (($scope.step2Form.$invalid) && (toState.data.step > fromState.data.step))
      event.preventDefault();
    $scope.hideBackButton = true;
  });

  $scope.$on('$destroy', validate);
})

.controller('Step3Ctrl', function($scope, sharedProperties) {
  $scope.step3Submitted = false;
  $scope.formData=sharedProperties.getProperty();

  if($scope.formData!==undefined){ 
    $scope.Access_Mobile_Number=$scope.formData.Access_Mobile_Number!==undefined ? $scope.formData.Access_Mobile_Number : '';  
    $scope.Email=$scope.formData.Email!==undefined ? $scope.formData.Email : '';  
    $scope.Work_Status=$scope.formData.Work_Status!==undefined ? $scope.formData.Work_Status : '';  
    $scope.Work_Other=$scope.formData.Work_Other!==undefined ? $scope.formData.Work_Other : '';  
    $scope.Company_Name=$scope.formData.Company_Name!==undefined ? $scope.formData.Company_Name : '';  
    $scope.Company_Type=$scope.formData.Company_Type!==undefined ? $scope.formData.Company_Type : '';  
    $scope.Work_Place=$scope.formData.Work_Place!==undefined ? $scope.formData.Work_Place : '';  
    $scope.Work_Since=$scope.formData.Work_Since!==undefined ? $scope.formData.Work_Since : '';  
    $scope.Referee_Name=$scope.formData.Referee_Name!==undefined ? $scope.formData.Referee_Name : '';  
    $scope.Referee_Phone_Number=$scope.formData.Referee_Phone_Number!==undefined ? $scope.formData.Referee_Phone_Number: '';  
  }


  $scope.submit = function() {
    $scope.step3Submitted = true;
    $scope.capture_inputs_values();
  };
})

.controller('Step3FormCtrl', function($scope, $rootScope, $timeout) {

  var validate = $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
    if (($scope.step3Form.$invalid) && (toState.data.step > fromState.data.step))
      event.preventDefault();
    $scope.hideBackButton = true;
  });

  $scope.$on('$destroy', validate);
})

.controller('DoneCtrl', function($scope, $rootScope, $ionicLoading, $ionicHistory, $cordovaGeolocation, $state, $cordovaCamera, PostService) {

    document.addEventListener("pause", onPause, false);

    function onPause() {
      // Handle the pause event
      cordova.plugins.backgroundMode.setEnabled(true);
    }

    document.addEventListener("resume", onResume, false);

    function onResume() {
        // Handle the resume event
        cordova.plugins.backgroundMode.setEnabled(false);
    }



    $scope.goToCamera = function (imageType) {
        var options = {
          quality: 75,
          destinationType: Camera.DestinationType.DATA_URL,
          sourceType: Camera.PictureSourceType.CAMERA,
          allowEdit: true,
          encodingType: Camera.EncodingType.JPEG,
          popoverOptions: CameraPopoverOptions,
          saveToPhotoAlbum: false
      };

        $cordovaCamera.getPicture(options).then(function (imageData) {

            var CapturedformData = JSON.parse(window.localStorage.getItem('formData'));
            CapturedformData[imageType]=imageData;
            window.localStorage.setItem('formData', JSON.stringify(CapturedformData) );

           if(imageType=='Client_Picture')
             $scope.Client_Picture =imageData;

           if(imageType=='Client_Identity')
             $scope.Client_Identity =imageData;

           if(imageType=='Client_Signature')
             $scope.Client_Signature =imageData;

        }, function (err) {
          console.log(err);
            // An error occured. Show a message to the user
        });
  };


  $scope.submitAccount = function(){
  $ionicLoading.show({
      template: 'Submiting Account...'
    });
    var formData = JSON.parse(window.localStorage.getItem('formData'));
    formData.Client_Identity=encodeURIComponent(formData.Client_Identity);
    formData.Client_Picture=encodeURIComponent(formData.Client_Picture);
    formData.Client_Signature=encodeURIComponent(formData.Client_Signature);

   /*var formData={
    Access_Mobile_Number:"0768344126",
    Birth_District:"ARUSHA",
    Birth_Region:"ARUSHA",
    Birth_Ward:"NGARENALO",
    Client_Identity:encodeURIComponent("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"),
    Client_Picture:encodeURIComponent("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"),
    Client_Signature:encodeURIComponent("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"),
    Company_Name:"TSPDF",
    Company_Type:"PRIVATE",
    Date_Of_Birth:"2017-05-12",
    Education:"Degree",
    Email:"dogo@janja.com",
    Father_Name:"JANJARO",
    First_Name:"DOGO",
    Gender:"Male",
    Identity_Number:"VOTER'S ID",
    Identity_Type:"National ID",
    Issue_Date:"2017-05-12",
    Issue_Place:"DAR ES SALAAM",
    Last_Name:"JANJA",
    Marriage:"Married",
    Middle_Name:"DUWEA",
    Mother_Name:"MARIANA",
    Nationality:"TANZANIAN",
    Number_of_Family_Members:"4",
    Mobile_Number:"255684467838",
    Referee_Name:"ENOCK MATO",
    Referee_Phone_Number:"0789561233",
    Residence_District:"KINONDONI",
    Residence_Region:"DAR ES SALAAM",
    Residence_Since:"2015",
    Residence_Ward:"SINZA",
    Work_Other:"SDF",
    Work_Place:"MAKUMBUSHO",
    Work_Since:"2016",
    Work_Status:"Employed",
    post_author:1,
   };

   PostService.doAccountOpening(formData)
    .then(function(account){
      console.log(account);
       //window.localStorage.removeItem('formData');
    },function(err){
      console.log(err);
      $scope.error = err;
      //window.localStorage.removeItem('formData');
    });*/


    $cordovaGeolocation.getCurrentPosition({timeout: 10000, enableHighAccuracy: false})
    .then(function (position) {
          formData.latitude=position.coords.latitude;
          formData.longitude=position.coords.longitude;
          PostService.doAccountOpening(formData)
          .then(function(account){
            console.log(account);
             window.localStorage.removeItem('formData');
          },function(err){
            console.log(err);
            $scope.error = err;
            window.localStorage.removeItem('formData');
          });
    }, function(err) {
          PostService.doAccountOpening(formData)
          .then(function(account){
            console.log(account);
             window.localStorage.removeItem('formData');
          },function(err){
            //err
            console.log(err);
            $scope.error = err;
            window.localStorage.removeItem('formData');
          });
    });
  };

  
  var validate = $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
    $ionicHistory.clearHistory();
  });

  $scope.$on('$destroy', validate);

})


// POST
.controller('PostCtrl', function($scope, post_data, $ionicLoading, PostService, AuthService, $ionicScrollDelegate) {
  $scope.post = post_data.post; 
  $scope.comments = _.map(post_data.post.comments, function(comment){
    if(comment.author){
      PostService.getUserGravatar(comment.author.id)
      .then(function(avatar){
        comment.user_gravatar = avatar;
      });
      return comment;
    }else{
      return comment;
    }
  });
  $ionicLoading.hide();

  $scope.sharePost = function(link){
    window.plugins.socialsharing.share('Check this post here: ', null, null, link);
  };

  $scope.addComment = function(){

    $ionicLoading.show({
      template: 'Submiting comment...'
    });

    PostService.submitComment($scope.post.id, $scope.new_comment)
    .then(function(data){
      if(data.status=="ok"){
        var user = AuthService.getUser();

        var comment = {
          author: {name: user.data.username},
          content:$scope.new_comment,
          date: Date.now(),
          user_gravatar : user.avatar,
          id: data.comment_id
        };
        $scope.comments.push(comment);
        $scope.new_comment = "";
        $scope.new_comment_id = data.comment_id;
        $ionicLoading.hide();
        // Scroll to new post
        $ionicScrollDelegate.scrollBottom(true);
      }
    });
  };
})


// CATEGORY
.controller('PostCategoryCtrl', function($scope, $rootScope, $state, $ionicLoading, $stateParams, PostService) {

  $scope.category = {};
  $scope.category.id = $stateParams.categoryId;
  $scope.category.title = $stateParams.categoryTitle;

  $scope.posts = [];
  $scope.page = 1;
  $scope.totalPages = 1;

  $scope.doRefresh = function() {
    $ionicLoading.show({
      template: 'Loading posts...'
    });

    PostService.getPostsFromCategory($scope.category.id, 1)
    .then(function(data){
      $scope.totalPages = data.pages;
      $scope.posts = PostService.shortenPosts(data.posts);

      $ionicLoading.hide();
      $scope.$broadcast('scroll.refreshComplete');
    });
  };

  $scope.loadMoreData = function(){
    $scope.page += 1;

    PostService.getRecentPosts($scope.category.id, $scope.page)
    .then(function(data){
      //We will update this value in every request because new posts can be created
      $scope.totalPages = data.pages;
      var new_posts = PostService.shortenPosts(data.posts);
      $scope.posts = $scope.posts.concat(new_posts);

      $scope.$broadcast('scroll.infiniteScrollComplete');
    });
  };

  $scope.moreDataCanBeLoaded = function(){
    return $scope.totalPages > $scope.page;
  };

  $scope.sharePost = function(link){
    PostService.sharePost(link);
  };

  $scope.bookmarkPost = function(post){
    $ionicLoading.show({ template: 'Post Saved!', noBackdrop: true, duration: 1000 });
    PostService.bookmarkPost(post);
  };

  $scope.doRefresh();
})


// WP PAGE
.controller('PageCtrl', function($scope, page_data) {
  $scope.page = page_data.page;
})

;
