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
        width: 2,
    });
    const [csvFile, setCsvFile] = useState({});


    const handleFileImport = (e) => {
        let csv_file = e.target.files[0];
        setCsvFile(csv_file)

    }

    //File Submit Handler
    const handleSubmitFile = (event) => {
        event.preventDefault();
        if (csvFile != null) {

            let formData = new FormData();
            formData.append('file', csvFile);
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

            <Stack direction="row"  alignItems="center"  justifyContent="center" spacing={3}>
            <Button
             size='medium'
                onChange={handleFileImport}
                component="label"
                role={undefined}
                variant="contained"
                startIcon={<CloudUploadIcon />}
            >
                Upload principals
                <VisuallyHiddenInput type="file" />
            </Button>
            
            <Button size='medium' onClick={handleSubmitFile} variant="contained" endIcon={<SendIcon />}>
                Import principals
            </Button>
            </Stack>

        </form>
    );

};

export default FileUpload;