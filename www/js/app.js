// Ionic Starter App

angular.module('underscore', [])
.factory('_', function() {
  return window._; // assumes underscore has already been loaded on the page
});

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
angular.module('your_app_name', [
  'ionic',
  'your_app_name.directives',
  'your_app_name.controllers',
  //'your_app_name.views',
  'your_app_name.services',
  'your_app_name.config',
  'your_app_name.factories',
  'your_app_name.filters',
  'ngMap',
  'angularMoment',
  'underscore',
  'ngCordova',
  'youtube-embed'
])

.run(function($ionicPlatform, AuthService, $rootScope, $state) {

  $ionicPlatform.on("deviceready", function(){

    AuthService.userIsLoggedIn().then(function(response)
    {
      if(response === true)
      {
        //update user avatar and go on
        AuthService.updateUserAvatar();

        $state.go('app.home');
      }
      else
      {
        $state.go('walkthrough');
      }
    });

    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if (window.cordova && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
    }

    if (window.StatusBar) {
      // org.apache.cordova.statusbar required
      StatusBar.styleDefault();
    }

  });

  $ionicPlatform.on("resume", function(){
    AuthService.userIsLoggedIn().then(function(response)
    {
      if(response === false)
      {
        $state.go('walkthrough');
      }else{
        //update user avatar and go on
        AuthService.updateUserAvatar();
      }
    });
  });

  // UI Router Authentication Check
  $rootScope.$on("$stateChangeStart", function(event, toState, toParams, fromState, fromParams){
    if (toState.data.authenticate){
      AuthService.userIsLoggedIn().then(function(response){
        if(response === false){
          event.preventDefault();
          $state.go('walkthrough');
        }
      });
    }
  });
})

.config(function($stateProvider, $urlRouterProvider) {
  $stateProvider

  .state('walkthrough', {
    url: "/",
    templateUrl: "views/auth/walkthrough.html",
    controller: 'WalkthroughCtrl',
    data: {
      authenticate: false
    }
  })

  .state('register', {
    url: "/register",
    templateUrl: "views/auth/register.html",
    controller: 'RegisterCtrl',
    data: {
      authenticate: false
    }
  })

  .state('login', {
    url: "/login",
    templateUrl: "views/auth/login.html",
    controller: 'LoginCtrl',
    data: {
      authenticate: false
    }
  })

  .state('forgot_password', {
    url: "/forgot_password",
    templateUrl: "views/auth/forgot-password.html",
    controller: 'ForgotPasswordCtrl',
    data: {
      authenticate: false
    }
  })

  .state('app', {
    url: "/app",
    abstract: true,
    templateUrl: "views/app/side-menu.html",
    controller: 'AppCtrl'
  })

  .state('app.preview', {
    url: "/preview",
    views: {
      'menuContent': {
        templateUrl: "views/app/preview.html",
        controller: 'SnapCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })

  .state('app.home', {
    url: "/home",
    views: {
      'menuContent': {
        templateUrl: "views/app/home.html",
        controller: 'HomeCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })

  .state('app.approved', {
    url: "/approved",
    views: {
      'menuContent': {
        templateUrl: "views/app/home.html",
        controller: 'ApprovedCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })

  .state('app.rejected', {
    url: "/rejected",
    views: {
      'menuContent': {
        templateUrl: "views/app/home.html",
        controller: 'RejectedCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })


  .state('app.step1', {
    url: "/step1",
    data: {
      step: 1
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/step1.html",
        controller: 'Step1Ctrl'
      }
    }
  })

  .state('app.step2', {
    url: "/step2",
    data: {
      step: 2
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/step2.html",
        controller: 'Step2Ctrl'
      }
    }
  })

  .state('app.step3', {
    url: "/step3",
    data: {
      step: 3
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/step3.html",
        controller: 'Step3Ctrl'
      }
    }
  })

  .state('app.done', {
    url: "/done",
    data: {
      step: 4
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/done.html",
        controller: 'DoneCtrl'
      }
    }
  })
.state('app.loan1', {
    url: "/loan1",
    data: {
      step: 1
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/loan1.html",
        controller: 'Loan1Ctrl'
      }
    }
  })

  .state('app.loan2', {
    url: "/loan2",
    data: {
      step: 2
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/loan2.html",
        controller: 'Loan2Ctrl'
      }
    }
  })

  .state('app.loan3', {
    url: "/loan3",
    data: {
      step: 3
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/loan3.html",
        controller: 'Loan3Ctrl'
      }
    }
  })

  .state('app.loan_done', {
    url: "/loan_done",
    data: {
      step: 4
    },
    views: {
      'menuContent': {
        templateUrl: "views/app/form-steps/loan_done.html",
        controller: 'DoneLoanCtrl'
      }
    }
  })

  .state('app.bookmarks', {
    url: "/bookmarks",
    views: {
      'menuContent': {
        templateUrl: "views/app/bookmarks.html",
        controller: 'BookMarksCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })

  .state('app.contact', {
    url: "/contact",
    views: {
      'menuContent': {
        templateUrl: "views/app/contact.html",
        controller: 'ContactCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })

  .state('app.post', {
    url: "/post/:postId",
    views: {
      'menuContent': {
        templateUrl: "views/app/wordpress/post.html",
        controller: 'PostCtrl'
      }
    },
    data: {
      authenticate: true
    },
    resolve: {
      post_data: function(PostService, $ionicLoading, $stateParams) {
        $ionicLoading.show({
      		template: 'Loading post ...'
      	});

        var postId = $stateParams.postId;
        return PostService.getPost(postId);
      }
    }
  })

  .state('app.settings', {
    url: "/settings",
    views: {
      'menuContent': {
        templateUrl: "views/app/settings.html",
        controller: 'SettingCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })

  .state('app.category', {
    url: "/category/:categoryTitle/:categoryId",
    views: {
      'menuContent': {
        templateUrl: "views/app/wordpress/category.html",
        controller: 'PostCategoryCtrl'
      }
    },
    data: {
      authenticate: true
    }
  })

  .state('app.page', {
    url: "/wordpress_page",
    views: {
      'menuContent': {
        templateUrl: "views/app/wordpress/wordpress-page.html",
        controller: 'PageCtrl'
      }
    },
    data: {
      authenticate: true
    },
    resolve: {
      page_data: function(PostService) {
        //You should replace this with your page slug
        var page_slug = 'wordpress-page';
        return PostService.getWordpressPage(page_slug);
      }
    }
  })

;
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/app/home');

});
