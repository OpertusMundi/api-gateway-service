import Vue from 'vue';
import VueRouter, { RouteConfig } from 'vue-router';

import store from '@/store';

import Home from '@/views/Home.vue';

Vue.use(VueRouter);

const routes: RouteConfig[] = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/catalogue/',
    name: 'Catalogue',
    component: () => import(/* webpackChunkName: "cataloguesingle" */ '../views/Catalogue.vue'),
  },
  {
    path: '/catalogue/:id',
    name: 'CatalogueSingle',
    component: () => import(/* webpackChunkName: "cataloguesingle" */ '../views/CatalogueSingle.vue'),
  },
  {
    path: '/terms',
    name: 'Terms',
    component: () => import(/* webpackChunkName: "terms" */ '../views/Terms.vue'),
  },
  {
    path: '/privacy',
    name: 'Privacy',
    component: () => import(/* webpackChunkName: "privacy" */ '../views/Privacy.vue'),
  },
  {
    path: '/faq',
    name: 'Faq',
    component: () => import(/* webpackChunkName: "faq" */ '../views/Faq.vue'),
  },
  {
    path: '/dashboard',
    component: () => import(/* webpackChunkName: "dashboardmain" */ '../views/dashboard/Main.vue'),
    meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
    children: [
      {
        path: '',
        name: 'DashboardHome',
        component: () => import(/* webpackChunkName: "dashboardhome" */ '../views/dashboard/Home.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'assets',
        name: 'Assets',
        component: () => import(/* webpackChunkName: "dashboardassets" */ '../views/dashboard/Assets.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'assets/create',
        name: 'CreateAsset',
        component: () => import(/* webpackChunkName: "dashboardcreateasset" */ '../views/dashboard/CreateAsset.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import(/* webpackChunkName: "dashboardprofile" */ '../views/dashboard/Profile.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import(/* webpackChunkName: "dashboardfavorites" */ '../views/dashboard/Favorites.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import(/* webpackChunkName: "dashboardmesssages" */ '../views/dashboard/Messages.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import(/* webpackChunkName: "dashboardsettings" */ '../views/dashboard/Settings.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'messages/create',
        name: 'MessageNew',
        component: () => import(/* webpackChunkName: "dashboardmesssagenew" */ '../views/dashboard/MessageNew.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
      {
        path: 'messages/:id',
        name: 'MessagesThread',
        component: () => import(/* webpackChunkName: "dashboardmesssagesthread" */ '../views/dashboard/MessagesThread.vue'),
        meta: { requiresRole: 'ROLE_USER', layout: 'dashboard' },
      },
    ],
  },
  {
    path: '/order-thankyou',
    name: 'OrderThankYou',
    component: () => import(/* webpackChunkName: "thankyou" */ '../views/orders/Thankyou.vue'),
  },
  {
    path: '/signin',
    name: 'Login',
    component: () => import(/* webpackChunkName: "login" */ '../views/Login.vue'),
    meta: {
      hideForAuth: true,
    },
  },
  {
    path: '/error/:error',
    name: 'Error',
    component: () => import(/* webpackChunkName: "error" */ '../views/ErrorPage.vue'),
  },
  {
    path: '/about',
    name: 'About',
    component: () => import(/* webpackChunkName: "about" */ '../views/About.vue'),
  },
  {
    path: '/vendor-benefits',
    name: 'VendorBenefits',
    component: () => import(/* webpackChunkName: "vendorbenefits" */ '../views/VendorBenefits.vue'),
  },
  {
    path: '/user-benefits',
    name: 'UserBenefits',
    component: () => import(/* webpackChunkName: "userbenefits" */ '../views/UserBenefits.vue'),
  },
  {
    path: '/admin',
    name: 'Admin',
    // route level code-splitting
    // this generates a separate chunk (admin.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "admin" */ '../views/Admin.vue'),
    meta: {
      requiresRole: 'ROLE_ADMIN',
    },
  },
  {
    path: '*',
    redirect: '/error/404',
  },
];

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes,
});

router.beforeEach((to, from, next) => {
  const role = to.meta?.requiresRole;
  const auth = to.meta?.hideForAuth;
  if (auth && store.getters.isAuthenticated) {
    next({ name: 'User' });
  }

  if (role && !store.getters.hasRole(role)) {
    next('/error/401');
  } else {
    next();
  }
});

export default router;
