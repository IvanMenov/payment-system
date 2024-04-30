import React, { Component } from 'react';
import { importPrincipals } from '../APIUtils';
import { styled } from '@mui/material/styles';
import Button from '@mui/material/Button';
import SendIcon from '@mui/icons-material/Send';
import Stack from '@mui/material/Stack';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { trackPromise } from 'react-promise-tracker';
import { useState } from 'react';
import './FileUpload.css'
import { Store } from 'react-notifications-component';


const FileUpload = (props) => {

    const VisuallyHiddenInput = styled('input')({
        clip: 'rect(0 0 0 0)',
        clipPath: 'inset(50%)',
        height: 1,
        overflow: 'hidden',
        position: 'absolute',
        bottom: 0,
        left: 0,
        whiteSpace: 'nowrap',
        width: 1,
    });
    const [csvFile, setCsvFile] = useState({});


    const handleFileImport = (e) => {
        let name = 'csv_file_to_import';
        let csv_file = e.target.files[0];
        setCsvFile(values => ({ ...values, [name]: csv_file }))

    }

    //File Submit Handler
    const handleSubmitFile = (event) => {
        event.preventDefault();
        if (csvFile.csv_file_to_import != null) {

            let formData = new FormData();
            formData.append('file', csvFile.csv_file_to_import);
            trackPromise(
                importPrincipals(formData)
                .then(response => {
                    setCsvFile({})
                    Store.addNotification({
                        message: "Principals successfull imported!",
                        type: "success",
                        insert: "top",
                        container: "top-right",
                        animationIn: ["animate__animated", "animate__fadeIn"],
                        animationOut: ["animate__animated", "animate__fadeOut"],
                        dismiss: {
                            duration: 3000,
                            onScreen: true
                        }
                    });
                }).catch(error => {
                    Store.addNotification({
                        message: "Problem importing principal!",
                        type: "danger",
                        insert: "top",
                        container: "top-right",
                        animationIn: ["animate__animated", "animate__fadeIn"],
                        animationOut: ["animate__animated", "animate__fadeOut"],
                        dismiss: {
                            duration: 3000,
                            onScreen: true
                        }
                    })
                }));
        } else {
            Store.addNotification({
                message: "Choose file before uploading!",
                type: "danger",
                insert: "top",
                container: "top-right",
                animationIn: ["animate__animated", "animate__fadeIn"],
                animationOut: ["animate__animated", "animate__fadeOut"],
                dismiss: {
                    duration: 3000,
                    onScreen: true
                }
            })
        }
    }


    return (
        <form onSubmit={handleSubmitFile}>

            <Stack direction="row" spacing={2}>
            <Button
                onChange={handleFileImport}
                component="label"
                role={undefined}
                variant="contained"
                tabIndex={-1}
                startIcon={<CloudUploadIcon />}
            >
                Upload file
                <VisuallyHiddenInput type="file" />
            </Button>
            
            <Button onClick={handleSubmitFile} variant="contained" endIcon={<SendIcon />}>
                Import Principals
            </Button>
            </Stack>

        </form>
    );

};

export default FileUpload;