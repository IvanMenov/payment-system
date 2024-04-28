import React from 'react'
import './Login.css'
import { Link, Redirect } from 'react-router-dom'
import { useState } from 'react';
import { ACCESS_TOKEN, PRINCIPAL } from '../../constants';
import Alert from '@mui/material/Alert';
import { login } from '../../APIUtils';
import FileUpload from '../FileUpload';
import { trackPromise } from 'react-promise-tracker';
import { Store } from 'react-notifications-component';

const Login = (props) => {

    const [input, setInput] = useState({})

    const handleSubmit = (event) => {
        event.preventDefault();
        if(input != null) {
            trackPromise(
            login(input)
                .then(response => {
                    localStorage.setItem(ACCESS_TOKEN, response.token);
                    props.history.push("/profile");
                    props.history.go();
                }).catch(error => {
                    Store.addNotification({
                        message: "Wrong username or password!",
                        type: "danger",
                        insert: "top",
                        container: "top-right",
                        animationIn: ["animate__animated", "animate__fadeIn"],
                        animationOut: ["animate__animated", "animate__fadeOut"],
                        dismiss: {
                          duration: 3000,
                          onScreen: true
                        }
                      });
                }));
        }else{
            Store.addNotification({
                message: "No username and password provided",
                type: "danger",
                insert: "top",
                container: "top-right",
                animationIn: ["animate__animated", "animate__fadeIn"],
                animationOut: ["animate__animated", "animate__fadeOut"],
                dismiss: {
                  duration: 5000,
                  onScreen: true
                }
              });
        }

    }

    const handleChange = (e) => {
        const name = e.target.name;

        const value = e.target.value;

        setInput(values => ({ ...values, [name]: value }))

    }

    if (props.authenticated) {
        return <Redirect
            to={{
                pathname: "/profile",
                state: { from: props.history.location }
            }} />;
    } else {
        return (

            <div className="login-container">

                <div className="login-content">
                    <h1 className="login-title">Login</h1>

                    <form onSubmit={handleSubmit}>
                        <div className="form-item">
                            <input type="email" name="email"
                                className="form-control" placeholder="Email"
                                value={input.email || ""} onChange={handleChange} required />
                        </div>
                        <div className="form-item">
                            <input type="password" name="password"
                                className="form-control" placeholder="Password"
                                value={input.password || ""} onChange={handleChange} required />
                        </div>
                        <div className="form-item ">
                            <button type="submit" className="btn btn-block btn-primary button-size">Login</button>
                        </div>
                    </form>
                    <div className="file-upload-padding"><FileUpload/></div>
                    
                </div>
            </div>
        );

    }

};

export default Login
