package torusworld.model.md3;


/** @modelguid {3CEAE588-287C-44C6-87FE-5A4A8749E9F1} */
public class AnimationSet {
	
    /** @modelguid {1074CF50-4DE1-4E0B-82D8-403085B8FEC1} */
	public String torsoAnimation;
    /** @modelguid {B2C29338-E176-460F-A3CB-1EFE5CC7425F} */
	public String legsAnimation;
	
    /** @modelguid {F80C76DA-36C9-427F-AED2-1AFEEC30CC26} */
	public AnimationSet(String torso, String legs){
		torsoAnimation = new String(torso);
		legsAnimation = new String(legs);
	}	
}
