/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.mapping;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A remote mapping context provides a model element with a view of the remote state
 * of local resources as they relate to a repository operation that is in
 * progress. A repository provider can pass an instance of this interface to a
 * model element when obtaining a set of traversals for a model element. This
 * allows the model element to query the remote state of a resource in order to
 * determine if there are resources that exist remotely but do not exist locally
 * that should be included in the traversal. 
 * <p>
 * This class may be subclassed by clients.
 * </p>
 * 
 * @see ResourceMapping
 * @see ResourceMappingContext
 * @since 3.2
 */
public abstract class RemoteResourceMappingContext extends ResourceMappingContext {
	
	/**
	 * Status code that is used to indicate that the context is
	 * unable to perform the requested operation because it would 
	 * require server contact but server contact is not permitted.
	 * For instance, server contact may not be possible in operations
	 * that must be short running.
	 */
	public static final int SERVER_CONTACT_PROHIBITED = -1;
	
	/**
	 * Status code that is used to indicate that invoked method 
	 * is not valid for the type of comparison supported by the remote 
	 * context. For instance, the <code>hasRemoteChanges</code>,
	 * <code>hasLocalChanges</code> and <code>fetchBaseContents</code>
	 * methods only apply to three-way comparisons.
	 */
	public static final int INVALID_FOR_COMPARISON_TYPE = -2;
	
    /**
     * Refresh flag constant (bit mask value 0) indicating that no
     * additional refresh behavior is required.
     */
    public static final int NONE = 0;
    
    /**
     * Refresh flag constant (bit mask value 1) indicating that
     * the mapping will be making use of the contents of the files
     * covered by the traversals being refreshed.
     */
    public static final int FILE_CONTENTS_REQUIRED = 1;

    /**
     * Return <code>true</code> if the context is associated 
     * with an operation that is using a three-way comparison
     * and <code>false</code> if it is using a two-way comparison.
     * @return whether the context is a three-way or two-way
     */
    public abstract boolean isThreeWay();
    
    /**
     * @deprecated to be removed after 3.2 M3. Use {@link #hasRemoteChange(IResource, IProgressMonitor)}.
	 */
    public boolean contentDiffers(IFile file, IProgressMonitor monitor) throws CoreException {
    	return hasRemoteChange(file, monitor);
    }
    
    /**
	 * For two-way comparisons, return whether the contents of the corresponding
	 * remote differs from the content of the local file in the context of the
	 * current operation. By this we mean that this method will return
	 * <code>true</code> if the remote contents differ from the local
	 * contents.
	 * <p>
	 * For three-way comparisons, return whether the contents of the
	 * corresponding remote differ from the contents of the base. In other
	 * words, this method returns <code>true</code> if the corresponding
	 * remote has changed since the last time the local resource was updated
	 * with the remote contents.
	 * <p>
	 * This can be used by clients to determine if they need to fetch the remote
	 * contents in order to determine if the resources that constitute the model
	 * element are different in the remote location. If the local file exists
	 * and the remote file does not, or the remote file exists and the local
	 * does not then the contents will be said to differ (i.e. <code>true</code>
	 * is returned). Also, implementors will most likely use a timestamp based
	 * comparison to determine if the contents differ. This may lead to a
	 * situation where <code>true</code> is returned but the actual contents
	 * do not differ. Clients must be prepared handle this situation.
	 * </p>
	 * 
	 * @param resource the local resource
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *            reporting is not desired
	 * @return whether the contents of the corresponding remote differ from the
	 *         base.
	 * @throws CoreException if the contents could not be compared. Reasons
	 *             include:
	 *             <ul>
	 *             <li>The server could not be contacted for some reason (e.g.
	 *             the context in which the operation is being called must be
	 *             short running). The status code will be
	 *             SERVER_CONTACT_PROHIBITED. </li>
	 *             <li>The corresponding remote resource is not a container
	 *             (status code will be IResourceStatus.RESOURCE_WRONG_TYPE).</li>
	 *             <li>The comparison type is two-way (status code will be
	 *             INVALID_FOR_COMPARISON_TYPE)
	 *             </ul>
	 */
    public abstract boolean hasRemoteChange(IResource resource, IProgressMonitor monitor) throws CoreException;
    
    /**
     * For three-way comparisons, this method indicates whether local
     * modifications have been made to the given resource.
     * @param resource the resource being tested
     * @param monitor a progress monitor
     * @return whether the resource contains local modifications
     * @throws CoreException
     */
    public abstract boolean hasLocalChange(IResource resource, IProgressMonitor monitor) throws CoreException;
    
    /**
	 * Returns an instance of IStorage in order to allow the caller to access
	 * the contents of the remote that corresponds to the given local resource.
	 * If the remote file does not exist, <code>null</code> is returned. The
	 * provided local file handle need not exist locally. A exception is thrown
	 * if the corresponding remote resource is not a file.
	 * <p>
	 * This method may be long running as a server may need to be contacted to
	 * obtain the contents of the file.
	 * </p>
	 * 
	 * @param file the local file
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *            reporting is not desired
	 * @return a storage that provides access to the contents of the local
	 *         resource's corresponding remote resource. If the remote file does
	 *         not exist, <code>null</code> is returned
	 * @throws CoreException if the contents could not be fetched. Reasons
	 *             include:
	 *             <ul>
	 *             <li>The server could not be contacted for some reason (e.g.
	 *             the context in which the operation is being called must be
	 *             short running). The status code will be
	 *             SERVER_CONTACT_PROHIBITED. </li>
	 *             <li>The corresponding remote resource is not a container
	 *             (status code will be IResourceStatus.RESOURCE_WRONG_TYPE).</li>
	 *             </ul>
	 */
    public abstract IStorage fetchRemoteContents(IFile file, IProgressMonitor monitor) throws CoreException;
    
    /**
	 * For three-way comparisons, returns an instance of IStorage in order to
	 * allow the caller to access the contents of the base resource that
	 * corresponds to the given local resource. The base of a resource is the
	 * contents of the resource before any local modifications were made. If the
	 * base file does not exist, <code>null</code> is returned. The provided
	 * local file handle need not exist locally. A exception is thrown if the
	 * corresponding base resource is not a file.
	 * <p>
	 * This method may be long running as a server may need to be contacted to
	 * obtain the contents of the file.
	 * </p>
	 * 
	 * @param file the local file
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *            reporting is not desired
	 * @return a storage that provides access to the contents of the local
	 *         resource's corresponding remote resource. If the remote file does
	 *         not exist, <code>null</code> is returned
	 * @throws CoreException if the contents could not be fetched. Reasons
	 *             include:
	 *             <ul>
	 *             <li>The server could not be contacted for some reason (e.g.
	 *             the context in which the operation is being called must be
	 *             short running). The status code will be
	 *             SERVER_CONTACT_PROHIBITED. </li>
	 *             <li>The corresponding remote resource is not a container
	 *             (status code will be IResourceStatus.RESOURCE_WRONG_TYPE).</li>
	 *             <li>The comparison type is two-way (status code will be
	 *             INVALID_FOR_COMPARISON_TYPE)
	 *             </ul>
	 */
    public abstract IStorage fetchBaseContents(IFile file, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the list of member resources whose corresponding remote resources
     * are members of the corresponding remote resource of the given local
     * container. The container need not exist locally and the result may
     * include entries that do not exist locally and may not include all local
     * children. An empty list is returned if the remote resource which
     * corresponds to the container is empty. A <code>null</code> is
     * returned if the remote does not exist. An exception is thrown if the
     * corresponding remote is not capable of having members.
     * <p>
     * This method may be long running as a server may need to be contacted to
     * obtain the members of the container's corresponding remote resource.
     * </p>
     * 
     * @param container the local container
     * @param monitor a progress monitor, or <code>null</code> if progress
     *    reporting is not desired
     * @return a list of member resources whose corresponding remote resources
     *    are members of the remote counterpart of the given container or
     *    <code>null</code> if the remote does not exist.
     * @throws CoreException if the members could not be fetched. Reasons include:
     * <ul>
     * <li>The server could not be contacted for some reason (e.g.
     *     the context in which the operation is being called must
     *     be short running). The status code will be SERVER_CONTACT_PROHIBITED.
     *    </li>
     * <li>The corresponding remote resource is not a container
     *    (status code will be IResourceStatus.RESOURCE_WRONG_TYPE).</li>
     * </ul>
     */
    public abstract IResource[] fetchMembers(IContainer container, IProgressMonitor monitor) throws CoreException;
        
    /**
	 * Refresh the known remote state for any resources covered by the given
	 * traversals. Clients who require the latest remote state should invoke
	 * this method before invoking any others of the class. Mappings can use
	 * this method as a hint to the context provider of which resources will be
	 * required for the mapping to generate the proper set of traversals.
	 * <p>
	 * Note that this is really only a hint to the context provider. It is up to
	 * implementors to decide, based on the provided traversals, how to
	 * efficiently perform the refresh. In the ideal case, calls to
	 * {@link #contentDiffers} and {@link #fetchMembers} would not need to
	 * contact the server after a call to a refresh with appropriate traversals.
	 * Also, ideally, if {@link #FILE_CONTENTS_REQUIRED} is on of the flags,
	 * then the contents for these files will be cached as efficiently as
	 * possible so that calls to {@link #fetchRemoteContents} will also not need to
	 * contact the server. This may not be possible for all context providers,
	 * so clients cannot assume that the above mentioned methods will not be
	 * long running. It is still advisably for clients to call {@link #refresh}
	 * with as much details as possible since, in the case where a provider is
	 * optimized, performance will be much better.
	 * </p>
	 * 
	 * @param traversals
	 *            the resource traversals which indicate which resources are to
	 *            be refreshed
	 * @param flags
	 *            additional refresh behavior. For instance, if
	 *            <code>FILE_CONTENTS_REQUIRED</code> is one of the flags,
	 *            this indicates that the client will be accessing the contents
	 *            of the files covered by the traversals. <code>NONE</code>
	 *            should be used when no additional behavior is required
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress
	 *            reporting is not desired
	 * @throws CoreException
	 *             if the refresh fails. Reasons include:
	 *             <ul>
	 *             <li>The server could not be contacted for some reason (e.g.
	 *             the context in which the operation is being called must be
	 *             short running). The status code will be
	 *             SERVER_CONTACT_PROHIBITED. </li>
	 *             </ul>
	 */
    public abstract void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException;
}