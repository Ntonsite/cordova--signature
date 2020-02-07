/* Developed by Ntonsite Mwamlima*/
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
      $scope.error = err;
      $ionicLoading.hide();
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

//Controller for fetching Loan data utilizing loan service

.controller('HomeCtrl', function($scope, $rootScope, $state, $ionicLoading, $timeout, LoanService) {
  $scope.posts = [];
  $scope.page = 1;
  $scope.totalPages = 1;

  $scope.doRefresh = function() {
    $ionicLoading.show({
      template: 'Loading accounts...'
    });

    //Always bring me the latest posts => page=1
    LoanService.getRecentPosts(1)
    .then(function(data){

      $scope.totalPages = data.pages;
      $scope.posts = LoanService.shortenPosts(data.posts);
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

    LoanService.getRecentPosts($scope.page)
    .then(function(data){
      //We will update this value in every request because new posts can be created
      $scope.totalPages = data.pages;
      var new_posts = LoanService.shortenPosts(data.posts);
      $scope.posts = $scope.posts.concat(new_posts);

      $scope.$broadcast('scroll.infiniteScrollComplete');
    });
  };

  $scope.moreDataCanBeLoaded = function(){
    return $scope.totalPages > $scope.page;
  };

  $scope.sharePost = function(link){
    LoanService.sharePost(link);
  };

  $scope.bookmarkPost = function(post){
    $ionicLoading.show({ template: 'Post Saved!', noBackdrop: true, duration: 1000 });
    LoanService.bookmarkPost(post);
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

    document.addEventListener("pause", onPause, true);

    function onPause() {
      // Handle the pause event
      cordova.plugins.backgroundMode.setEnabled(true);
    }

    document.addEventListener("resume", onResume, true);

    function onResume() {
        // Handle the resume event
        cordova.plugins.backgroundMode.setEnabled(true);
    }
    $scope.goToCam = function (imageType) {
        var options = {
          quality: 75,
          destinationType: Camera.DestinationType.DATA_URL,
          sourceType: Camera.PictureSourceType.CAMERA,
          allowEdit: true,
          encodingType: Camera.EncodingType.JPEG,
          popoverOptions: CameraPopoverOptions,
          saveToPhotoAlbum: true
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


    /*NOW let's validate the functionality it-self*/

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
        window.localStorage.setItem('formData');
      });
    }, function(err) {
        PostService.doAccountOpening(formData)
        .then(function(account){
          console.log(account);
           window.localStorage.setItem('formData');
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


//controllers for Loan-form-steps
.controller('Loan1Ctrl', function($scope, sharedProperties) {
  $scope.formData=sharedProperties.getProperty();
  console.log($scope.formData);
  $scope.step1Submitted = false;



  if($scope.formData!==undefined){
    $scope.First_Name=$scope.formData.First_Name!==undefined ? $scope.formData.First_Name : '';
    $scope.Middle_Name=$scope.formData.Middle_Name!==undefined ? $scope.formData.Middle_Name : '';
    $scope.Last_Name=$scope.formData.Last_Name!==undefined ? $scope.formData.Last_Name : '';
    $scope.Mobile_Number=$scope.formData.Mobile_Number!==undefined ? $scope.formData.Mobile_Number : '';
    $scope.email=$scope.formData.email!==undefined ? $scope.formData.email : '';
    $scope.mteja=$scope.formData.mteja!==undefined ? $scope.formData.mteja : '';
    $scope.Birth_Region=$scope.formData.Birth_Region!==undefined ? $scope.formData.Birth_Region : '';
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

.controller('Loan2Ctrl', function($scope, sharedProperties) {
  $scope.step2Submitted = false;
  $scope.formData=sharedProperties.getProperty();
  console.log($scope.formData);

  if($scope.formData!==undefined){

    $scope.biashara=$scope.formData.biashara!==undefined ? $scope.formData.biashara : '';
    $scope.muda_biashara=$scope.formData.muda_biashara!==undefined ? $scope.formData.muda_biashara : '';
    $scope.eneo_biashara=$scope.formData.eneo_biashara!==undefined ? $scope.formData.eneo_biashara : '';

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

.controller('Loan3Ctrl', function($scope, sharedProperties) {
  $scope.step3Submitted = false;
  $scope.formData=sharedProperties.getProperty();

  if($scope.formData!==undefined){
    $scope.kiasi_mkopo = $scope.formData.kiasi_mkopo!==undefined ? $scope.formData.kiasi_mkopo : '';
    $scope.ukomo_mkopo = $scope.formData.ukomo_mkopo!==undefined ? $scope.formData.ukomo_mkopo : '';
    $scope.mkopo_dhumuni = $scope.formData.mkopo_dhumuni!==undefined ? $scope.formData.mkopo_dhumuni : '';

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

.controller('DoneLoanCtrl', function($scope, $rootScope, $ionicLoading, $ionicHistory, $cordovaGeolocation, $state, $cordovaCamera, LoanService) {

    document.addEventListener("pause", onPause, true);

    function onPause() {
      // Handle the pause event
      cordova.plugins.backgroundMode.setEnabled(true);
    }

    document.addEventListener("resume", onResume, true);

    function onResume() {
        // Handle the resume event
        cordova.plugins.backgroundMode.setEnabled(true);
    }
    $scope.goToCamera = function (imageType) {
        var options = {
          quality: 20,
          destinationType: Camera.DestinationType.DATA_URL,
          sourceType: Camera.PictureSourceType.CAMERA,
          allowEdit: true,
          encodingType: Camera.EncodingType.JPEG,
          popoverOptions: CameraPopoverOptions,
          saveToPhotoAlbum: true
      };

        $cordovaCamera.getPicture(options).then(function (imageData) {

            var CapturedformData = JSON.parse(window.localStorage.getItem('formData'));
            CapturedformData[imageType]=imageData;
            window.localStorage.setItem('formData', JSON.stringify(CapturedformData));

           if(imageType=='Loan_Identity')
             $scope.Loan_Identity =imageData;

           if(imageType=='Loan_Signature')
             $scope.Loan_Signature =imageData;

        }, function (err) {
          alert('Failed because: ' + err);
          console.log(err);
            // An error occured. Show a message to the user
        });
  };

  $scope.submitLoan = function(){
  $ionicLoading.show({
      template: 'Submiting Loan Request...'
    });

    var formData = JSON.parse(window.localStorage.getItem('formData'));
    formData.Loan_Identity=encodeURIComponent(formData.Loan_Identity);

    formData.Loan_Signature=encodeURIComponent(formData.Loan_Signature);

    $cordovaGeolocation.getCurrentPosition({timeout: 10000, enableHighAccuracy: false})
    .then(function (position) {
      formData.latitude=position.coords.latitude;
      formData.longitude=position.coords.longitude;
      LoanService.doAccountOpening(formData)
      .then(function(account){
        console.log(account);
         window.localStorage.removeItem('formData');
      },function(err){
        console.log(err);
        $scope.error = err;
        window.localStorage.setItem('formData');
      });
    }, function(err) {
        LoanService.doAccountOpening(formData)
        .then(function(account){
          console.log(account);
           window.localStorage.setItem('formData');
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
});
