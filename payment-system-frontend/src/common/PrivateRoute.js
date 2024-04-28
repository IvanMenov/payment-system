import React from 'react'
import {
  Route,
  Redirect
} from "react-router-dom";


function PrivateRoute({ component: Component, authenticated, ...rest }) {
  return (
    <Route {...rest} render={(props) => {
      return authenticated === true
        ? <Component {...rest} {...props} />
        :
        <Redirect to={{
          pathname: '/login',
          state: { from: props.location }
        }}
        ></Redirect>
    }
    } />
  )
}

export default PrivateRoute
