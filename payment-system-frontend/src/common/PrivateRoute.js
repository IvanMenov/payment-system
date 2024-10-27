import React from 'react'
import { Redirect, Route } from 'react-router-dom';

const PrivateRoute = ({isAuthenticated,  component: Component, ...rest }) => {
  return (
    <Route
      {...rest}
      render={() =>
        isAuthenticated ? ( // If authenticated, render the component
          <Component {...rest} />
        ) : (
          <Redirect to="/" /> // Otherwise, redirect to the home page or login page
        )
      }
    />
  );
};


export default PrivateRoute
