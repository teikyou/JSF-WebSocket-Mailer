/*
* Copyright 2013 Yoshio Terada
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package jp.co.oracle.samples.tasks;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import jp.co.oracle.samples.beans.FolderName;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Yoshio Terada
 */
public class AllFolderHandlerTask implements Callable<TreeNode> {

    private final Store store;
    private static final Logger logger = Logger.getLogger(AllFolderHandlerTask.class.getPackage().getName());
    
    public AllFolderHandlerTask(Store store) {
        this.store = store;
    }

    @Override
    public TreeNode call() throws Exception {
        TreeNode root = new DefaultTreeNode("root", null);
        Folder[] folders;
        if (store == null) {
            return null;
        }
        try {
            folders = store.getDefaultFolder().list();
            getAllIMAPFolders(root, folders);
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return root;
    }
 
    private TreeNode getAllIMAPFolders(TreeNode root, Folder[] folders) {
        TreeNode child = null;
        try {
            for (Folder folder : folders) {
                String folName = folder.getName();
                String folFullName = folder.getFullName();
                if (hasChildFolder(folder) == true) {
                    child = new DefaultTreeNode(new FolderName(folName, folFullName), root);
                    getAllIMAPFolders(child, folder.list());
                } else {
                    child = new DefaultTreeNode(new FolderName(folName, folFullName), root);

                }
            }
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return child;
    }

    //フォルダに子のフォルダがあるか否か
    private boolean hasChildFolder(Folder folder) throws MessagingException {
        boolean hasFolder = false;
        if (folder.list().length > 0) {
            hasFolder = true;
        }
        return hasFolder;
    }
}
