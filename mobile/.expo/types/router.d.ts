/* eslint-disable */
import * as Router from 'expo-router';

export * from 'expo-router';

declare module 'expo-router' {
  export namespace ExpoRouter {
    export interface __routes<T extends string | object = string> {
      hrefInputParams: { pathname: Router.RelativePathString, params?: Router.UnknownInputParams } | { pathname: Router.ExternalPathString, params?: Router.UnknownInputParams } | { pathname: `/`; params?: Router.UnknownInputParams; } | { pathname: `/_sitemap`; params?: Router.UnknownInputParams; } | { pathname: `/trip/activities`; params?: Router.UnknownInputParams; } | { pathname: `/trip/details`; params?: Router.UnknownInputParams; } | { pathname: `/trip/[id]`, params: Router.UnknownInputParams & { id: string | number; } };
      hrefOutputParams: { pathname: Router.RelativePathString, params?: Router.UnknownOutputParams } | { pathname: Router.ExternalPathString, params?: Router.UnknownOutputParams } | { pathname: `/`; params?: Router.UnknownOutputParams; } | { pathname: `/_sitemap`; params?: Router.UnknownOutputParams; } | { pathname: `/trip/activities`; params?: Router.UnknownOutputParams; } | { pathname: `/trip/details`; params?: Router.UnknownOutputParams; } | { pathname: `/trip/[id]`, params: Router.UnknownOutputParams & { id: string; } };
      href: Router.RelativePathString | Router.ExternalPathString | `/${`?${string}` | `#${string}` | ''}` | `/_sitemap${`?${string}` | `#${string}` | ''}` | `/trip/activities${`?${string}` | `#${string}` | ''}` | `/trip/details${`?${string}` | `#${string}` | ''}` | { pathname: Router.RelativePathString, params?: Router.UnknownInputParams } | { pathname: Router.ExternalPathString, params?: Router.UnknownInputParams } | { pathname: `/`; params?: Router.UnknownInputParams; } | { pathname: `/_sitemap`; params?: Router.UnknownInputParams; } | { pathname: `/trip/activities`; params?: Router.UnknownInputParams; } | { pathname: `/trip/details`; params?: Router.UnknownInputParams; } | `/trip/${Router.SingleRoutePart<T>}` | { pathname: `/trip/[id]`, params: Router.UnknownInputParams & { id: string | number; } };
    }
  }
}
